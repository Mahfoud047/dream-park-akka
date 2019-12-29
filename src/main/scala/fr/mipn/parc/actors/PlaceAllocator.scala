package fr.mipn.parc.actors

import akka.actor.{Actor, Props}
import io.swagger.server.model.{Place, PlaceType}


object PlaceAllocator {

  var places: Map[Int, Place] = Map(
    1 -> Place(1, "A", 0, PlaceType("voitures", "desc de type voitures")),
    2 -> Place(2, "A", 0, PlaceType("voitures", "desc de type voitures")),
    3 -> Place(3, "B", 0, PlaceType("voitures", "desc de type voitures")),
    4 -> Place(4, "B", 0, PlaceType("lord", "desc de type lord")),
    5 -> Place(5, "B", 0, PlaceType("velo", "desc de type lord"))
  )

  case object GetFreePlaces

  def apply(): Props = Props(new PlaceAllocator())
}


case class PlaceAllocator() extends Actor {

  import PlaceAllocator._

  override def receive: Receive = {

    case GetFreePlaces => {
      val freePlaces: List[Place] = places.filter({
        case (_, place) => place.status == 0
      }).values.toList
      sender ! Right(freePlaces)
    }

    case _@msg => sender ! s"I recieved the msg $msg"
  }
}
