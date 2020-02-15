package fr.mipn.parc.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import io.swagger.server.model.{ErrorResponse, Payment, ReservationBody, PostSuccessResponse, Reservation, SettleReservationResponse}
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

  //    var lastId: Int = 0;
  var reservations: Map[Int, Reservation] = Map(
    // empty
  ).withDefaultValue(null)

  case class ReservePlace(reservationRequest: ReservationBody)

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

  override def preStart() {
    // get places from file reservations.json
    FileHelpers.readData(
      FileHelpers.RESERVATIONS_FILE_PATH
    )(reservationJsonFormatProtocol) match {
      case None => log.info("no place")

      case Some(places) => initPlaces(places)
    }
  }


  /**
   *
   * @param listReservations
   */
  def initPlaces(listReservations: List[Reservation]): Unit = {
    listReservations.foreach(res =>
      reservations += (res.id -> res)
    )
  }


  /**
   *
   * @param onSuccess
   * @param onError
   */
  def saveReservations(
                        onSuccess: () => Unit,
                        onError: () => Unit
                      ): Unit = {
    FileHelpers.writeData(
      FileHelpers.RESERVATIONS_FILE_PATH,
      reservations.values.toList
    )(reservationJsonFormatProtocol) match {
      case Some(err) => onError()
      case None => if (onSuccess != null) onSuccess()
    }
  }

  /**
   *
   * @return
   */
  def setPlaceFree(sender: ActorRef, placeId: Int): Future[Unit] = {
    (placeAllocator ? PlaceAllocator.FreePlace(placeId))
      .mapTo[OptionAllocatePlace]
      .map {
        case Some(err) => sender ! Some(err)
      }
  }

  def updateReservation(sender: ActorRef, newReservation: Reservation, callback: () => Unit): Unit = {
    // get Reservation by Id

    val reservation = reservations.find(r => r._1 == newReservation.id).orNull._2

    if (reservation == null) {
      sender ! Some(ErrorResponse("reservation not found"))
    }

    reservations = reservations.updated(newReservation.id, newReservation)


    //Save Reservations
    saveReservations(
      callback,
      () => sender ! Some(ErrorResponse("Internal Error"))
    )


  }


  /**
   *
   * @param sender
   */
  def saveReservations(sender: ActorRef,
                       callback: () => Unit
                      ): Unit = {
    FileHelpers.writeData(
      FileHelpers.RESERVATIONS_FILE_PATH,
      reservations.values.toList
    )(reservationJsonFormatProtocol) match {
      case Some(err) => sender ! Some(ErrorResponse("Internal Error"))
      case None => if (callback != null) callback()
    }
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


      val lastId: Int = if (reservations.isEmpty) 1 else reservations.keys.last + 1

      val newReservation = reservePlace.reservationRequest.toReservation(id = lastId)

      reservations += (lastId -> newReservation)

      // persist
      saveReservations(
        () => client ! Right(PostSuccessResponse(lastId)),
        () => client ! Left(ErrorResponse("Internal Error"))
      )


    /**
     * *********
     */
    case GetAllReservations =>
      sender ! Right(reservations.values.toList)


    /**
     * *********
     */

    case cancelReservation: CancelReservation =>
      val client: ActorRef = sender

      val reservation: Reservation =
        reservations.find(r => r._1 == cancelReservation.idReservation).map(_._2).orNull

      if (reservation == null) {
        client ! Some(ErrorResponse("reservation not found"))
      } else {
        // Set Place Free
        setPlaceFree(client, reservation.placeId)

        //Delete Reservation
        reservations = reservations.filterKeys(_ != reservation.id)
        saveReservations(
          () => client ! None,
          () => client ! Some(ErrorResponse("Internal Error"))
        )
      }


    /**
     * *********
     */

    case settleReservation: SettleReservation =>

      val client: ActorRef = sender

      // get the reservaton
      val id = settleReservation.reservationId
      val reservation = reservations.find(_._1 == id).map(_._2).orNull

      if (reservation == null) {
        client ! Left(ErrorResponse("reservation not found"))
      } else {
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
              updateReservation(
                client,
                newReservation,
                () => client ! Right(SettleReservationResponse(fee, wallet - fee))
              )

          }

      }


    /**
     * *********
     */

    case _
      @msg => sender ! s"I recieved the msg $msg"
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



