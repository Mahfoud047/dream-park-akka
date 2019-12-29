package fr.mipn.parc.actors

import akka.actor.{Actor, Props}


object FeeCalculator {
  def apply(): Props = Props(new FeeCalculator())
}

case class FeeCalculator() extends Actor {
  override def receive: Receive = {
    case _@msg => sender ! s"I recieved the msg $msg"
  }
}
