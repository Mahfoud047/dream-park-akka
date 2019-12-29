package fr.mipn.parc.actors

import akka.actor.{Actor, ActorRef, Props}
import fr.mipn.parc.actors.ParkingSupervisor.GetFreePlaces
import io.swagger.server.model.Place
import akka.pattern.ask


/*
Diagramme de séquence du Parc

ParkingSupervisor   ReservationScheduler             PlaceAllocator        FeeCalculator
  |                        |                               |                     |
  |------------------------CheckPricingPlans  ---------------------------------->|
  |<------------------PricingPlans(plans)----------------------------------------|
  |                        |                               |                     |
  |------------------------CheckFreePlaces---------------->|                     |
  |<--------------------FreePlaces(places)-----------------|                     |
  |                        |                               |                     |
  |                        |                               |                     |
  |-ReservePlace(place, -->|                               |                     |
  |        plan)           |--CheckPlaceIsFree(place)----->|                     |
  |                        |<-ResponsePlaceIsFree(isFree)--|                     |
  |                        |                               |                     |
  |                   if(!isFree)___________               |                     |
  |<-ResponseReservePlace--|                               |                     |
  |(Error("place not free")|                               |                     |
  |                        |________________               |                     |
  |                       else______________               |                     |
  |                        |                               |                     |
  |                        |--AllocatePlace(place)-------->|                     |
  |                        |                               |                     |
  |                        |<-ResponseAllocatePlace--------|                     |
  |                        |                               |                     |
  |<-ResponseReservePlace--|                               |                     |
  |(OK)                    |________________               |                     |
  |                        |                               |                     |
  |                        |                               |                     |
  |                        |                               |                     |
  |                        |                               |                     |
  |-CancelReservation( --> |                               |                     |
  |        reservation)
  |                        |--FreePlace(place)------------>|                     |
  |                        |                               |                     |
  |                        |<-ResponseFreePlace------------|                     |
  |<-ResponseCancel  ------|                               |                     |
  | Reservation(OK)        |                               |                     |
  |                        |                               |                     |
  |                        |                               |                     |
  |                        |                               |                     |
  |-SettleReservation( --> |                               |                     |
  | reservation, payment); |                               |                     |
  |                        |                               |                     |
  |                        |--CalculateReservationFee(reservation)-------------->|
  |                        |                               |                     |
  |                        |                               |                     |
  |                        |<------ResponseReservationFee(fee)-------------------|
  |                        |                               |                     |
  |                    if(payment < fee)____________       |                     |
  |<-ResponseSettleReservation-|                           |                     |
  |(Error("not enough money")|                             |                     |
  |                        |                               |                     |
  |                       else______________               |                     |
  |                        |                               |                     |
  |                        |-FreePlace(reservation.place)->|                     |
  |                        |<-ResponseFreePlace------------|                     |
  |                        |                               |                     |
  |<-ResponseSettleReservation(OK)-|                       |                     |

*/


object ParkingSupervisor {

  case object GetFreePlaces

  def apply(feeCalculator: ActorRef, placeAllocator: ActorRef, reservationScheduler: ActorRef): Props
  = Props(new ParkingSupervisor(feeCalculator, placeAllocator, reservationScheduler))

}

case class ParkingSupervisor(feeCalculator: ActorRef, placeAllocator: ActorRef, reservationScheduler: ActorRef) extends Actor {

//  case class FreePlaces(places: Either[Error, List[Place]])


  override def receive: Receive = {

//    case GetFreePlaces => {
//      sender ! placeAllocator ? CheckFreePlaces(sender).mapTo[]
//    }
//    case FreePlaces(client, places) => places.map(
//      {
//        case Right(list) => client !
//      })

    case _@msg => sender ! s"I recieved the msg $msg"
  }
}
