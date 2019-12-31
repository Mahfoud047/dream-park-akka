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

import scala.concurrent.duration._


object ReservationScheduler {

  var lastId: Int = 0;
  var reservations: Map[Int, Reservation] = Map(
    // empty
  ).withDefaultValue(null)

  case class ReservePlace(reservation: PostReservation)

  case object GetAllReservations

  def apply(feeCalculator: ActorRef, placeAllocator: ActorRef): Props =
    Props(new ReservationScheduler(feeCalculator, placeAllocator))

}

case class ReservationScheduler(feeCalculator: ActorRef, placeAllocator: ActorRef) extends Actor with  ActorLogging{

  import ReservationScheduler._

  implicit val timeout = new Timeout(2 seconds)
  implicit val executionContext = this.context.system.dispatcher

  override def receive: Receive = {
    case reservePlace: ReservePlace =>
      // check pricing
      (feeCalculator ? FeeCalculator.CheckPricingExists(reservePlace.reservation.pricingPlanName))
        .mapTo[EitherCheckPricingExists]
        .map {
          case Left(err) => sender ! Left(err) // if error return it directly
          case Right(false) => sender ! Left(Error("pricing plan not found")) // if not found
        }

      // allocate place
      (placeAllocator ? PlaceAllocator.AllocatePlace(reservePlace.reservation.placeId))
        .mapTo[OptionAllocatePlace]
        .map {
          case Some(err) => sender ! Left(err)
        }

      lastId += 1
      reservations += (lastId -> reservePlace.reservation.toReservation(id = lastId))

      sender ! Right(Ok)



    case GetAllReservations =>
      log.info("recieved GetAllReservations")


      object reservationJsonFormatProtocol extends DefaultJsonProtocol {
        implicit val format = listFormat(jsonFormat7(Reservation))
      }

      val reservations = FileHelpers.readData(
        FileHelpers.RESERVATIONS_PATH
      )(reservationJsonFormatProtocol)

      reservations match {
        case None =>  sender ! Left(Error("error"))
        case Some(value) => sender ! Right(value)
      }

    case _@msg => sender ! s"I recieved the msg $msg"
  }
}
