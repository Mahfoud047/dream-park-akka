package fr.mipn.parc.actors

import akka.actor.{Actor, ActorRef, Props}


object ReservationScheduler {

  def apply(feeCalculator: ActorRef, placeAllocator: ActorRef): Props =
    Props(new ReservationScheduler(feeCalculator, placeAllocator))

}

case class ReservationScheduler(feeCalculator: ActorRef, placeAllocator: ActorRef) extends Actor {
  override def receive: Receive = {
    case _@msg => sender ! s"I recieved the msg $msg"
  }
}
