package fr.mipn.parc.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import io.swagger.server.model.{Error, Reservation}
import akka.pattern.ask
import akka.util.Timeout
import fr.mipn.parc.ResponseTypes.{EitherCheckPricingExists, OptionAllocatePlace}
import io.swagger.server.input_model.PostReservation
import io.swagger.server.utils.FileHelpers
import io.swagger.server.utils.FileHelpers.jsonFormat7
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

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

  override def receive: Receive = {
    case reservePlace: ReservePlace =>

      val pricingName = reservePlace.reservationRequest.pricingPlanName
      val placeId = reservePlace.reservationRequest.placeId

      val checkPricingExistsFuture : Future[EitherCheckPricingExists] = (feeCalculator ? FeeCalculator.CheckPricingExists(pricingName)).mapTo[EitherCheckPricingExists]
      val allocatePlaceFuture : Future[OptionAllocatePlace] = (placeAllocator ? PlaceAllocator.AllocatePlace(placeId)).mapTo[OptionAllocatePlace]

      for {
        checkPricingExistsResult <- checkPricingExistsFuture
        allocatePlaceResult <- allocatePlaceFuture
      } yield {
        checkPricingExistsResult match {
          case Left(err) => sender ! Left(err) // if error return it directly
          case Right(false) => sender ! Left(Error("Bad input")) // if not found
        }
        allocatePlaceResult match {
          case Some(err) => sender ! Left(err)
        }
      }


      FileHelpers.readData(
        FileHelpers.RESERVATIONS_FILE_PATH
      )(reservationJsonFormatProtocol) match {
        case None => sender ! Left(Error("Internal Error"))

        case Some(reservations) =>
          val lastId: Int = if (reservations.isEmpty) 1 else reservations.last.id + 1

          val newReservation = reservePlace.reservationRequest.toReservation(id = lastId)

          val finalData = reservations :+ newReservation

          FileHelpers.writeData(FileHelpers.RESERVATIONS_FILE_PATH, finalData)(reservationJsonFormatProtocol)
          match {
            case Some(err) => sender ! Error("Internal Error")
            case None => sender ! Right(Ok)
          }

      }


    case GetAllReservations =>

      FileHelpers.readData(
        FileHelpers.RESERVATIONS_FILE_PATH
      )(reservationJsonFormatProtocol) match {

        case None => sender ! Left(Error("Internal Error"))

        case Some(reservations) => sender ! Right(reservations)

      }

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



