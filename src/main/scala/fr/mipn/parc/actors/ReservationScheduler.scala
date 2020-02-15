package fr.mipn.parc.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import io.swagger.server.model.{ErrorResponse, Payment, PostReservation, PostSuccessResponse, Reservation, SettleReservationResponse}
import akka.pattern.ask
import akka.util.Timeout
import fr.mipn.parc.ResponseTypes.{EitherCheckPricingExists, OptionAllocatePlace, OptionCalculateFee}
import io.swagger.server.utils.{DateFormatter, FileHelpers}
import org.joda.time.DateTime
import spray.json.DefaultJsonProtocol
import DateFormatter.formatDate

import scala.concurrent.Future
import scala.concurrent.duration._


object ReservationScheduler {

  //  var lastId: Int = 0;
  //  var reservations: Map[Int, Reservation] = Map(
  //    // empty
  //  ).withDefaultValue(null)

  case class ReservePlace(reservationRequest: PostReservation)

  case class ResponseGeneralCheckReservation(
                                              existsPricing: EitherCheckPricingExists,
                                              placeAllocated: OptionAllocatePlace
                                            )

  case class CancelReservation(idReservation: Int)

  case class SettleReservation(payment: Payment, reservationId: Int)


  case object GetAllReservations

  def apply(feeCalculator: ActorRef, placeAllocator: ActorRef): Props =
    Props(new ReservationScheduler(feeCalculator, placeAllocator))

}

case class ReservationScheduler(feeCalculator: ActorRef, placeAllocator: ActorRef) extends Actor with ActorLogging {

  import ReservationScheduler._

  implicit val timeout = new Timeout(2 seconds)
  implicit val executionContext = this.context.system.dispatcher

  object reservationJsonFormatProtocol extends DefaultJsonProtocol {
    implicit val format = listFormat(jsonFormat7(Reservation))
  }

  /**
   *
   * @param callback
   * @param sender
   */
  def getAllReservations(sender: ActorRef, callback: (List[Reservation]) => Unit): Unit = {
    FileHelpers.readData(
      FileHelpers.RESERVATIONS_FILE_PATH
    )(reservationJsonFormatProtocol) match {
      case None => sender ! Left(ErrorResponse("Internal Error"))

      case Some(reservations) => callback(reservations)
    }
  }


  /**
   *
   * @param idReservation
   * @return
   */
  def getReservationById(idReservation: Int): Option[Reservation] = {
    FileHelpers.readData(
      FileHelpers.RESERVATIONS_FILE_PATH
    )(reservationJsonFormatProtocol) match {
      case None => None
      case Some(reservations) => reservations.find(r => r.id == idReservation)
    }
  }


  /**
   *
   * @param sender
   * @param content
   */
  def saveReservations(sender: ActorRef,
                       content: List[Reservation],
                       callback: () => Unit
                      ): Unit = {
    FileHelpers.writeData(
      FileHelpers.RESERVATIONS_FILE_PATH,
      content
    )(reservationJsonFormatProtocol) match {
      case Some(err) => sender ! Some(ErrorResponse("Internal Error"))
      case None => if (callback != null) callback()
    }
  }

  /**
   *
   * @return
   */
  def setPlaceFree(sender: ActorRef, placeId: Int) = {
    (placeAllocator ? PlaceAllocator.FreePlace(placeId))
      .mapTo[OptionAllocatePlace]
      .map {
        case Some(err) => sender ! Some(err)
      }
  }

  def updateReservation(sender: ActorRef, newReservation: Reservation) = {
    getAllReservations(
      sender,
      (reservations: List[Reservation]) => {
        // get Reservation by Id
        val reservation = reservations.find(r => r.id == newReservation.id).orNull

        if (reservation == null) {
          sender ! Some(ErrorResponse("reservation not found"))
        }

        //Save Reservation
        val newReservations = reservations.map((r => if (r.id == newReservation.id) newReservation else r))
        saveReservations(sender, newReservations, null)

      }
    )
  }

  override def receive: Receive = {
    /**
     * *********
     */
    case reservePlace: ReservePlace =>
      val client: ActorRef = sender

      val pricingName = reservePlace.reservationRequest.pricingPlanName
      val placeId = reservePlace.reservationRequest.placeId

      val checkPricingExistsFuture: Future[EitherCheckPricingExists] = (feeCalculator ? FeeCalculator.CheckPricingExists(pricingName)).mapTo[EitherCheckPricingExists]
      val allocatePlaceFuture: Future[OptionAllocatePlace] = (placeAllocator ? PlaceAllocator.AllocatePlace(placeId)).mapTo[OptionAllocatePlace]

      for {
        checkPricingExistsResult <- checkPricingExistsFuture
        allocatePlaceResult <- allocatePlaceFuture
      } yield {
        checkPricingExistsResult match {
          case Left(err) => client ! Left(err) // if error return it directly
          case Right(false) => client ! Left(ErrorResponse("Bad input")) // if not found
        }
        allocatePlaceResult match {
          case Some(err) => client ! Left(err)
        }
      }


      getAllReservations(
        client,
        (reservations: List[Reservation]) => {
          val lastId: Int = if (reservations.isEmpty) 1 else reservations.last.id + 1

          val newReservation = reservePlace.reservationRequest.toReservation(id = lastId)

          val finalData = reservations :+ newReservation

          FileHelpers.writeData(FileHelpers.RESERVATIONS_FILE_PATH, finalData)(reservationJsonFormatProtocol)
          match {
            case Some(err) => client ! ErrorResponse("Internal Error")
            case None => client ! Right(PostSuccessResponse(lastId))
          }
        })

    /**
     * *********
     */
    case GetAllReservations =>

      getAllReservations(
        sender,
        (reservations: List[Reservation]) => {
          sender ! Right(reservations)
        }
      )

    /**
     * *********
     */

    case cancelReservation: CancelReservation =>
      val client: ActorRef = sender

      getAllReservations(
        client,
        (reservations: List[Reservation]) => {
          // get Place Id
          val reservation = reservations.find(r => r.id == cancelReservation.idReservation).orNull

          if (reservation == null) {
            client ! Some(ErrorResponse("reservation not found"))
          }

          // Set Place Free
          setPlaceFree(client, reservation.placeId)

          //Delete Reservation
          val newReservations = reservations.filter(_.id != reservation.id)
          saveReservations(client, newReservations, () => client ! None)

        }
      )


    /**
     * *********
     */

    case settleReservation: SettleReservation =>

      val client: ActorRef = sender

      // get the reservaton
      val reservation = getReservationById(settleReservation.reservationId).orNull

      if (reservation == null) {
        client ! Left(ErrorResponse("reservation not found"))
      }

      val startT = DateFormatter.parseDate(reservation.startTime).orNull
      if (startT == null) {
        client ! Left(ErrorResponse("invalid date"))
      }


      // calculate duration of reservation
      (feeCalculator ? FeeCalculator.CalculateFee(startT, reservation.pricingPlanName)).mapTo[OptionCalculateFee]
        .map {
          case None => client ! Left(ErrorResponse)
          case Some(fee) =>
            // check wallet
            val wallet = settleReservation.payment.wallet
            if (wallet < fee) {
              client ! Left(ErrorResponse("wallet not enough"))
            }
            // setPlace Free
            setPlaceFree(client, reservation.placeId)

            //update reservation date
            val now = DateTime.now()
            val newReservation = reservation.copy(endTime = formatDate(now))
            updateReservation(client, newReservation)

            // return fee
            client ! Right(SettleReservationResponse(fee, wallet - fee))
        }


    /**
     * *********
     */

    case _@msg => sender ! s"I recieved the msg $msg"
  }
}


//object DedicatedCheckReservation {
//
//  case class ResponseCheckPricingExists(response: EitherCheckPricingExists)
//
//  case class ResponseAllocatePlace(response: OptionAllocatePlace)
//
//  def apply(
//             feeCalculator: ActorRef,
//             placeAllocator: ActorRef,
//             pricingPlanName: String,
//             placeId: Int
//           ): Props =
//    Props(new DedicatedCheckReservation(feeCalculator, placeAllocator, pricingPlanName, placeId))
//
//}
//
//case class DedicatedCheckReservation(
//                                      feeCalculator: ActorRef,
//                                      placeAllocator: ActorRef,
//                                      pricingPlanName: String,
//                                      placeId: Int
//                                    ) extends Actor with ActorLogging {
//
//  import DedicatedCheckReservation._
//
//  override def preStart() = {
//    feeCalculator ! FeeCalculator.CheckPricingExists(pricingPlanName)
//    placeAllocator ! PlaceAllocator.AllocatePlace(placeId)
//  }
//
//
//  override def receive: Receive = {
//    case msg: ResponseCheckPricingExists => context.become(waitingForAllocatePlace(msg.response))
//    case msg: ResponseAllocatePlace => context.become(waitingForCheckPricingExists(msg.response))
//  }
//
//
//  def waitingForAllocatePlace(pricingExistsResponse: EitherCheckPricingExists): Receive = {
//    case msg: ResponseAllocatePlace => sendFinalResponse(pricingExistsResponse, msg.response)
//  }
//
//  def waitingForCheckPricingExists(allocatePlaceResponse: OptionAllocatePlace): Receive = {
//    case msg: ResponseCheckPricingExists => sendFinalResponse(msg.response, allocatePlaceResponse)
//  }
//
//  def sendFinalResponse(pricingExistsResponse: EitherCheckPricingExists,
//                        allocatePlaceResponse: OptionAllocatePlace) = {
//    context.parent ! ReservationScheduler.ResponseGeneralCheckReservation(pricingExistsResponse, allocatePlaceResponse)
//  }
//
//}



