package fr.mipn.parc

import akka.actor.{ActorRef, ActorSystem}
import fr.mipn.parc.actors.{FeeCalculator, ParkingSupervisor, PlaceAllocator, ReservationScheduler}

class ParkSystem(system: ActorSystem) {
  val feeCalculator: ActorRef = system.actorOf(FeeCalculator())
  val placeAllocator: ActorRef = system.actorOf(PlaceAllocator())
  val reservationScheduler: ActorRef = system.actorOf(ReservationScheduler(feeCalculator, placeAllocator))
//  val parkingSupervisor: ActorRef = system.actorOf(ParkingSupervisor(feeCalculator, placeAllocator, reservationScheduler))
}
