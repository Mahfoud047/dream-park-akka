package io.swagger.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Directives.{complete, pathPrefix}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import fr.mipn.parc.ParkSystem
import io.swagger.server.api.{DefaultApi, DefaultApiMarshaller, DefaultApiService}
import io.swagger.server.model.{Place, PlaceType, PricingPlan, Reservation}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import fr.mipn.parc.ResponseTypes._
import fr.mipn.parc.actors.PlaceAllocator


object DreamParkApp extends App {

  implicit val system: ActorSystem = ActorSystem()
  val parkSystem = new ParkSystem(system)

  // needed to run the route
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher


  object DefaultMarshaller extends DefaultApiMarshaller with SprayJsonSupport {

    import spray.json.DefaultJsonProtocol._

    override implicit def toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]] = listFormat(jsonFormat4(Place))

    override implicit def toEntityMarshallerError: ToEntityMarshaller[model.Error] = jsonFormat1(model.Error)

    override implicit def toEntityMarshallerPricingPlanarray: ToEntityMarshaller[List[PricingPlan]] = listFormat(jsonFormat5(PricingPlan))

    override implicit def toEntityMarshallerReservation: ToEntityMarshaller[Reservation] = jsonFormat6(Reservation)
  }


  object DefaultService extends DefaultApiService {


    implicit val timeout = new Timeout(2 seconds)

    /**
     * Code: 200, Message: List of availlable places, DataType: List[Place]
     * Code: 422, Message: Unexpected error, DataType: Error
     */
    override def placesFreeGet()(implicit toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]], toEntityMarshallerError: ToEntityMarshaller[model.Error]): Route = {

      val response = (parkSystem.placeAllocator ? PlaceAllocator.GetFreePlaces).mapTo[EitherArrayPlace]

      requestcontext =>
        response.flatMap {
          case Right(res: List[Place])
          => placesFreeGet200(res)(toEntityMarshallerPlacearray)(requestcontext)
          case Left(err: Error)
          => placesFreeGet422(model.Error(err.getMessage))(toEntityMarshallerError)(requestcontext)
        }

    }

    /**
     * Code: 200, Message: List of places, DataType: List[Place]
     * Code: 422, Message: Unexpected error, DataType: Error
     */
    override def placesGet()(implicit toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]], toEntityMarshallerError: ToEntityMarshaller[model.Error]): Route = ???

    /**
     * Code: 200, Message: List of possible prices, DataType: List[PricingPlan]
     * Code: 422, Message: Unexpected error, DataType: Error
     */
    override def pricesGet()(implicit toEntityMarshallerPricingPlanarray: ToEntityMarshaller[List[PricingPlan]], toEntityMarshallerError: ToEntityMarshaller[model.Error]): Route = ???

    /**
     * Code: 204, Message: OK
     * Code: 422, Message: Unexpected error, DataType: Error
     */
    override def reservationIdReservationDelete(idReservation: Int)(implicit toEntityMarshallerError: ToEntityMarshaller[model.Error]): Route = ???

    /**
     * Code: 200, Message: a reservation object, DataType: Reservation
     * Code: 422, Message: Unexpected error, DataType: Error
     */
    override def reservationIdReservationGet(idReservation: Int)(implicit toEntityMarshallerReservation: ToEntityMarshaller[Reservation], toEntityMarshallerError: ToEntityMarshaller[model.Error]): Route = ???

    /**
     * Code: 204, Message: OK
     * Code: 422, Message: Unexpected error, DataType: Error
     */
    override def reservationIdReservationSettlePut(idReservation: Int)(implicit toEntityMarshallerError: ToEntityMarshaller[model.Error]): Route = ???

    /**
     * Code: 201, Message: OK
     * Code: 422, Message: Unexpected error, DataType: Error
     */
    override def reservationPost(body: String)(implicit toEntityMarshallerError: ToEntityMarshaller[model.Error]): Route = ???
  }


  // Start the API
  val api = new DefaultApi(DefaultService, DefaultMarshaller)

  val host = "localhost"
  val port = 8888

  val bindingFuture = Http().bindAndHandle(pathPrefix("api") {
    api.route
  }, host, port)
  println(s"Server online at http://${host}:${port}/\nPress RETURN to stop...")

}