package fr.mipn.parc

import akka.actor.{ActorRef, ActorSystem}
import fr.mipn.parc.actors.{FeeCalculator, ParkingSupervisor, PlaceAllocator, ReservationScheduler}

/*
Diagramme de séquence du Parc

Client   ReservationScheduler             PlaceAllocator        FeeCalculator
  |                        |                               |                     |
  |------------------------GetPricingPlans  ---------------------------------->|
  |<------------------PricingPlans(plans)----------------------------------------|
  |                        |                               |                     |
  |------------------------GetFreePlaces---------------->|                     |
  |<--------------------FreePlaces(places)-----------------|                     |
  |                        |                               |                     |
  |                        |                               |                     |
  |-ReservePlace(reservation)->|                           |                     |
  |                        |                               |                     |
  |                        |                               |                     |
  |                        | --CheckPricingExists(reservation.pricingId)-------->|
  |                        |                               |                     |
  |                        |<-----------ResponseCheckPricingExists(exists)-------|
  |                        |                               |                     |
  |                    if(!exists)___________              |                     |
  |<-ResponseReservePlace--|                               |                     |
  |(Error("plan not free") |                               |                     |
  |                        |                               |                     |
  |                       else______________               |                     |
  |                        |-AllocatePlace(reservation.placeId)->|               |
  |                        |                               |                     |
  |                        |<-ResponseAllocatePlace--------|                     |
  |                        |                               |                     |
  |                   if(Error)___________                 |                     |
  |                        |<-ResponseAllocatePlace(Error)-|
  |<-ResponseReservePlace--|                               |                     |
  |(Error("place not free")|                               |                     |
  |                        |________________               |                     |
  |                       else______________               |                     |
  |                        |                               |                     |
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

class ParkSystem(system: ActorSystem) {
  val feeCalculator: ActorRef = system.actorOf(FeeCalculator())
  val placeAllocator: ActorRef = system.actorOf(PlaceAllocator())
  val reservationScheduler: ActorRef = system.actorOf(ReservationScheduler(feeCalculator, placeAllocator))
//  val parkingSupervisor: ActorRef = system.actorOf(ParkingSupervisor(feeCalculator, placeAllocator, reservationScheduler))
}
