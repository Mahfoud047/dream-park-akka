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
import io.swagger.server.model.{Error, Payment, Place, PricingPlan, Reservation}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import fr.mipn.parc.ResponseTypes._
import fr.mipn.parc.actors.{FeeCalculator, PlaceAllocator, ReservationScheduler}
import io.swagger.server.input_model.PostReservation
import spray.json.{DefaultJsonProtocol, RootJsonFormat}


object DreamParkApp extends App {

  implicit val system: ActorSystem = ActorSystem()
  val parkSystem = new ParkSystem(system)

  // needed to run the route
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val timeout = new Timeout(2 seconds)


  object DefaultMarshaller extends DefaultApiMarshaller with SprayJsonSupport {

    import DefaultJsonProtocol._

    override implicit def toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]] = listFormat(jsonFormat4(Place))

    override implicit def toEntityMarshallerError: ToEntityMarshaller[Error] = jsonFormat1(Error)

    override implicit def toEntityMarshallerPricingPlanarray: ToEntityMarshaller[List[PricingPlan]] = listFormat(jsonFormat5(PricingPlan))

    override implicit def toEntityMarshallerReservation: ToEntityMarshaller[Reservation] = jsonFormat7(Reservation)

    implicit val reservationFormat: RootJsonFormat[PostReservation] = jsonFormat6(PostReservation)

    override implicit def fromRequestUnmarshallerPayment: RootJsonFormat[Payment] = jsonFormat1(Payment)

    override implicit def toEntityMarshallerReservationarray: ToEntityMarshaller[List[Reservation]] = listFormat(jsonFormat7(Reservation))
  }


  object DefaultService extends DefaultApiService {


    /**
     * Code: 200, Message: List of possible prices, DataType: List[PricingPlan]
     * Code: 422, Message: Unexpected error, DataType: Error
     */
    override def pricingGet()(implicit toEntityMarshallerPricingPlanarray: ToEntityMarshaller[List[PricingPlan]], toEntityMarshallerError: ToEntityMarshaller[model.Error]): Route = {
      val response = (parkSystem.feeCalculator ? FeeCalculator.GetPricingPlans).mapTo[EitherArrayPricingPlan]

      requestcontext =>
        response.flatMap {
          case Right(res)
          => pricingGet200(res)(toEntityMarshallerPricingPlanarray)(requestcontext)
          case Left(err: Error)
          => pricingGet422(err)(toEntityMarshallerError)(requestcontext)
        }
    }

    /**
     * Code: 200, Message: List of available places, DataType: List[Place]
     * Code: 422, Message: Unexpected error, DataType: Error
     */
    override def placeFreeGet()(implicit toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]], toEntityMarshallerError: ToEntityMarshaller[Error]): Route = {
      val response = (parkSystem.placeAllocator ? PlaceAllocator.GetFreePlaces).mapTo[EitherArrayPlace]

      requestcontext =>
        response.flatMap {
          case Right(res)
          => placeFreeGet200(res)(toEntityMarshallerPlacearray)(requestcontext)
          case Left(err: Error)
          => placeFreeGet422(err)(toEntityMarshallerError)(requestcontext)
        }
    }


    /**
     * Code: 200, Message: List of places, DataType: List[Place]
     * Code: 422, Message: Unexpected error, DataType: Error
     */
    override def placeGet()(implicit toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]], toEntityMarshallerError: ToEntityMarshaller[Error]): Route = ???

    /**
     * Code: 200, Message: a reservation object, DataType: List[Reservation]
     * Code: 422, Message: Unexpected error, DataType: Error
     */
    override def reservationGet()(implicit toEntityMarshallerReservationarray: ToEntityMarshaller[List[Reservation]], toEntityMarshallerError: ToEntityMarshaller[Error]): Route = {

      val response = (parkSystem.reservationScheduler ? ReservationScheduler.GetAllReservations).mapTo[EitherArrayReservation]

      requestcontext => {
        response.flatMap {
          case Right(reservations)
          => reservationGet200(reservations)(toEntityMarshallerReservationarray)(requestcontext)
          case Left(err: Error)
          => reservationGet422(err)(toEntityMarshallerError)(requestcontext)
        }
      }

    }

    /**
     * Code: 204, Message: OK
     * Code: 422, Message: Unexpected error, DataType: Error
     */
    override def reservationReservationIdDelete(reservationId: Int)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route = {

      val response = (parkSystem.reservationScheduler ? ReservationScheduler.CancelReservation(reservationId)).mapTo[OptionFreePlace]

      requestcontext => {
        response.flatMap {
          case None
          => reservationReservationIdDelete204(requestcontext)
          case Some(err: Error)
          => reservationReservationIdDelete422(err)(toEntityMarshallerError)(requestcontext)
        }
      }
    }

    /**
     * Code: 200, Message: a reservation object, DataType: Reservation
     * Code: 422, Message: Unexpected error, DataType: Error
     */
    override def reservationReservationIdGet(reservationId: Int)(implicit toEntityMarshallerReservation: ToEntityMarshaller[Reservation], toEntityMarshallerError: ToEntityMarshaller[Error]): Route = ???

    /**
     * Code: 204, Message: OK
     * Code: 400, Message: Bad Request, DataType: Error
     * Code: 422, Message: Unexpected error, DataType: Error
     */
    override def reservationReservationIdSettlePut(body: Payment, reservationId: Int)(implicit toEntityMarshallerBody: ToEntityMarshaller[Payment], toEntityMarshallerError: ToEntityMarshaller[Error]): Route = ???

    /**
     * Code: 204, Message: OK
     * Code: 400, Message: Bad Request, DataType: Error
     * Code: 422, Message: Unexpected error, DataType: Error
     */
    override def reservationPost(body: PostReservation)
                                (implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route = {

      val response = (parkSystem.reservationScheduler ? ReservationScheduler.ReservePlace(body)).mapTo[EitherPostReservation]

      requestcontext => {
        response.flatMap {
          case Right(_)
          => reservationPost201(requestcontext)
          case Left(Error("Bad input"))
          => reservationPost400(Error("Bad input"))(toEntityMarshallerError)(requestcontext)
          case Left(err: Error)
          => reservationPost422(err)(toEntityMarshallerError)(requestcontext)
        }
      }
    }
  }


  // Start the API
  val api = new DefaultApi(DefaultService, DefaultMarshaller)

  val host = "localhost"
  val port = 8888

  val bindingFuture = Http().bindAndHandle(pathPrefix("api") {
    api.route
  }, host, port)
  println(s"Server online at http://${
    host
  }:${
    port
  }/\nPress RETURN to stop...")

}