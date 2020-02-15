package fr.mipn.parc.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.swagger.server.enums.PlaceStatus
import io.swagger.server.enums.PlaceStatus.PlaceStatus
import io.swagger.server.model.{ErrorResponse, Place, PlaceType}
import io.swagger.server.utils.FileHelpers
import spray.json.DefaultJsonProtocol


object PlaceAllocator {

  val placeTypes = List(PlaceType(1, "voitures", "desc de type voitures"),
    PlaceType(2, "velo", "desc de type velo"))

  var places: Map[Int, Place] = Map(
    // to read from file

    //    1 -> Place(1, "A", 1),
    //    2 -> Place(2, "A", 1),
    //    3 -> Place(3, "B", 2),
    //    4 -> Place(4, "B", 2),
    //    5 -> Place(5, "B", 2)
  ).withDefaultValue(null)

  case object GetFreePlaces

  case object GetAllPlaces

  case class AllocatePlace(placeId: Int)

  case class FreePlace(placeId: Int)

  def apply(): Props = Props(new PlaceAllocator())
}

object placeJsonFormatProtocol extends DefaultJsonProtocol {
  implicit val format = listFormat(jsonFormat4(Place))
}


case class PlaceAllocator() extends Actor with ActorLogging {

  import PlaceAllocator._

  override def preStart() {
    // get places from file places.json
    FileHelpers.readData(
      FileHelpers.PLACES_FILE_PATH
    )(placeJsonFormatProtocol) match {
      case None =>  log.info("no place")

      case Some(places) => initPlaces(places)
    }
  }

  def savePlaces(): Unit = {
    FileHelpers.writeData(
      FileHelpers.PLACES_FILE_PATH,
      places.values.toList
    )(placeJsonFormatProtocol)
  }

  def initPlaces(listPlaces: List[Place]): Unit = {
    log.info("init places")
    listPlaces.foreach(p => {
      places = places + (p.id -> p)
    })
  }

  def getPlace(idPlace: Int): Option[Place] = {
    // Check if the place exists
    val place: Place = places(idPlace)
    Option(place)
  }


  def updatePlaceStatus(sender: ActorRef, placeId: Int, newStatus: PlaceStatus): Unit = {
    getPlace(placeId) match {
      case None => sender ! Some(ErrorResponse("Place not found"))
      case Some(place) =>
        // Update Status
        places += (placeId -> place.copy(status = newStatus))
        // persist status
        savePlaces()
        sender ! None
    }
  }

  override def receive: Receive = {

    case GetFreePlaces =>
      val freePlaces: List[Place] = places.filter({
        case (_, place) => place.status == PlaceStatus.FREE
      }).values.toList
      sender ! Right(freePlaces)

    case GetAllPlaces =>
      val freePlaces: List[Place] = places.values.toList
      sender ! Right(freePlaces)


    case allocatePlace: AllocatePlace =>
      updatePlaceStatus(sender, allocatePlace.placeId, PlaceStatus.TAKEN)

    case freePlace: FreePlace =>
      updatePlaceStatus(sender, freePlace.placeId, PlaceStatus.FREE)

    case _@msg => sender ! s"I recieved the msg $msg"
  }
}
