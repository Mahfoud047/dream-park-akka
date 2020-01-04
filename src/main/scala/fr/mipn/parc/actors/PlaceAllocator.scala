package fr.mipn.parc.actors

import akka.actor.{Actor, ActorLogging, Props}
import io.swagger.server.enums.PlaceStatus
import io.swagger.server.model.{Error, Place, PlaceType}


object PlaceAllocator {

  val placeTypes = List(PlaceType(1, "voitures", "desc de type voitures"),
    PlaceType(2, "velo", "desc de type velo"))

  var places: Map[Int, Place] = Map(
    1 -> Place(1, "A", 1),
    2 -> Place(2, "A", 1),
    3 -> Place(3, "B", 2),
    4 -> Place(4, "B", 2),
    5 -> Place(5, "B", 2)
  ).withDefaultValue(null)

  case object GetFreePlaces

  case class AllocatePlace(placeId: Int)

  def apply(): Props = Props(new PlaceAllocator())
}


case class PlaceAllocator() extends Actor with ActorLogging {

  import PlaceAllocator._

  override def receive: Receive = {

    case GetFreePlaces =>
      val freePlaces: List[Place] = places.filter({
        case (_, place) => place.status == PlaceStatus.FREE
      }).values.toList
      sender ! Right(freePlaces)


    case allocatePlace: AllocatePlace =>

      // Check if the place exists
      val placeId = allocatePlace.placeId
      val place: Place = places(placeId)
      if (place == null) {
        sender ! Some(Error("Place not found"))
      } else {
        // Update Status
        places += (placeId -> place.copy(status = PlaceStatus.TAKEN))
        sender ! None
      }


    case _@msg => sender ! s"I recieved the msg $msg"
  }
}
