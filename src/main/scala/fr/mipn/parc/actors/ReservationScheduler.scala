package fr.mipn.parc.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import io.swagger.server.model.{Error, Reservation}
import akka.pattern.ask
import akka.util.Timeout
import fr.mipn.parc.ResponseTypes.{EitherCheckPricingExists, OptionAllocatePlace}
import io.swagger.server.input_model.PostReservation

import scala.concurrent.duration._


object ReservationScheduler {

  var lastId: Int = 0;
  var reservations: Map[Int, Reservation] = Map(
    // empty
  ).withDefaultValue(null)

  case class ReservePlace(reservation: PostReservation)

  def apply(feeCalculator: ActorRef, placeAllocator: ActorRef): Props =
    Props(new ReservationScheduler(feeCalculator, placeAllocator))

}

case class ReservationScheduler(feeCalculator: ActorRef, placeAllocator: ActorRef) extends Actor {

  import ReservationScheduler._

  implicit val timeout = new Timeout(2 seconds)
  implicit val executionContext = this.context.system.dispatcher

  override def receive: Receive = {
    case reservePlace: ReservePlace =>
      // check pricing
      (feeCalculator ? FeeCalculator.CheckPricingExists(reservePlace.reservation.pricingPlanName))
        .mapTo[EitherCheckPricingExists]
        .map {
          case Left(err) => Left(err) // if error return it directly
          case Right(false) => Left(Error("pricing plan not found")) // if not found
        }

      // allocate place
      (placeAllocator ? PlaceAllocator.AllocatePlace(reservePlace.reservation.placeId))
        .mapTo[OptionAllocatePlace]
        .map {
          case Some(err) => Left(err)
        }

      lastId += 1
      reservations += (lastId -> reservePlace.reservation.toReservation(id = lastId))

      Right(Ok)

    case _@msg => sender ! s"I recieved the msg $msg"
  }
}
