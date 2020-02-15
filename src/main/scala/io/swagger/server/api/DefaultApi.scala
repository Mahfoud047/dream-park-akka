package io.swagger.server.api

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.swagger.server.model.{ErrorResponse, Payment, Place, ReservationBody, PostSuccessResponse, PricingPlan, Reservation, SettleReservationResponse}
import spray.json.RootJsonFormat
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.{Directive0, Route}

import scala.concurrent.duration._

/**
 * From https://dzone.com/articles/handling-cors-in-akka-http
 * and https://ali.actor
 */
trait CORSHandler {

  private val corsResponseHeaders = List(
    `Access-Control-Allow-Origin`.*,
    `Access-Control-Allow-Credentials`(true),
    `Access-Control-Allow-Headers`("Authorization",
      "Content-Type", "X-Requested-With"),
    `Access-Control-Max-Age`(1.day.toMillis) //Tell browser to cache OPTIONS requests
  )


  //this directive adds access control headers to normal responses
  private def addAccessControlHeaders: Directive0 = {
    respondWithHeaders(corsResponseHeaders)
  }

  //this handles preflight OPTIONS requests.
  private def preflightRequestHandler: Route = options {
    complete(HttpResponse(StatusCodes.OK).
      withHeaders(`Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE)))
  }

  // Wrap the Route with this method to enable adding of CORS headers
  def corsHandler(r: Route): Route = addAccessControlHeaders {
    preflightRequestHandler ~ r
  }

  // Helper method to add CORS headers to HttpResponse
  // preventing duplication of CORS headers across code
  def addCORSHeaders(response: HttpResponse): HttpResponse =
    response.withHeaders(corsResponseHeaders)
}


class DefaultApi(
                  defaultService: DefaultApiService,
                  defaultMarshaller: DefaultApiMarshaller
                ) extends SprayJsonSupport with CORSHandler {

  import defaultMarshaller._

  private val cors = new CORSHandler {}

  lazy val route: Route = {
    cors.corsHandler(
      path("place" / "free") {
        get {


          defaultService.placeFreeGet()


        }
      } ~
        path("place") {
          get {


            defaultService.placeGet()


          }
        } ~
        path("pricing") {
          get {


            defaultService.pricingGet()


          }
        } ~
        path("reservation") {
          get {


            defaultService.reservationGet()


          }
        } ~
        path("reservation") {
          post {

            entity(as[ReservationBody]) {
              body => defaultService.reservationPost(body = body)
            }

          }
        } ~
        path("reservation" / IntNumber) { (reservationId) =>
          delete {


            defaultService.reservationReservationIdDelete(reservationId = reservationId)


          }
        } ~
        path("reservation" / IntNumber) { (reservationId) =>
          get {


            defaultService.reservationReservationIdGet(reservationId = reservationId)


          }
        } ~
        path("reservation" / IntNumber / "settle") { (reservationId) =>
          put {


            entity(as[Payment]) { body =>
              defaultService.reservationReservationIdSettlePut(body = body, reservationId = reservationId)
            }


          }
        }
    )
  }
}


trait DefaultApiService {

  def placeFreeGet200(responsePlacearray: List[Place])(implicit toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]]): Route =
    complete((200, responsePlacearray))

  def placeFreeGet422(responseError: ErrorResponse)(implicit toEntityMarshallerError: ToEntityMarshaller[ErrorResponse]): Route =
    complete((422, responseError))

  /**
   * Code: 200, Message: List of available places, DataType: List[Place]
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def placeFreeGet()
                  (implicit toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]], toEntityMarshallerError: ToEntityMarshaller[ErrorResponse]): Route

  def placeGet200(responsePlacearray: List[Place])(implicit toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]]): Route =
    complete((200, responsePlacearray))

  def placeGet422(responseError: ErrorResponse)(implicit toEntityMarshallerError: ToEntityMarshaller[ErrorResponse]): Route =
    complete((422, responseError))

  /**
   * Code: 200, Message: List of places, DataType: List[Place]
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def placeGet()
              (implicit toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]], toEntityMarshallerError: ToEntityMarshaller[ErrorResponse]): Route

  def pricingGet200(responsePricingPlanarray: List[PricingPlan])(implicit toEntityMarshallerPricingPlanarray: ToEntityMarshaller[List[PricingPlan]]): Route =
    complete((200, responsePricingPlanarray))

  def pricingGet422(responseError: ErrorResponse)(implicit toEntityMarshallerError: ToEntityMarshaller[ErrorResponse]): Route =
    complete((422, responseError))

  /**
   * Code: 200, Message: OK, DataType: List[PricingPlan]
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def pricingGet()
                (implicit toEntityMarshallerPricingPlanarray: ToEntityMarshaller[List[PricingPlan]], toEntityMarshallerError: ToEntityMarshaller[ErrorResponse]): Route

  def reservationGet200(responseReservationarray: List[Reservation])(implicit toEntityMarshallerReservationarray: ToEntityMarshaller[List[Reservation]]): Route =
    complete((200, responseReservationarray))

  def reservationGet422(responseError: ErrorResponse)(implicit toEntityMarshallerError: ToEntityMarshaller[ErrorResponse]): Route =
    complete((422, responseError))

  /**
   * Code: 200, Message: a list of reservation object, DataType: List[Reservation]
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def reservationGet()
                    (implicit toEntityMarshallerReservationarray: ToEntityMarshaller[List[Reservation]], toEntityMarshallerError: ToEntityMarshaller[ErrorResponse]): Route

  def reservationPost201(response: PostSuccessResponse)
                        (implicit toEntityMarshallerPostSuccessResponse: ToEntityMarshaller[PostSuccessResponse]): Route =
    complete((201, response))

  def reservationPost400(responseError: ErrorResponse)(implicit toEntityMarshallerError: ToEntityMarshaller[ErrorResponse]): Route =
    complete((400, responseError))

  def reservationPost422(responseError: ErrorResponse)(implicit toEntityMarshallerError: ToEntityMarshaller[ErrorResponse]): Route =
    complete((422, responseError))

  /**
   * Code: 201, Message: Reservation added
   * Code: 400, Message: Bad Request, DataType: Error
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def reservationPost(body: ReservationBody)(
    implicit toEntityMarshallerPostSuccessResponse: ToEntityMarshaller[PostSuccessResponse],
    toEntityMarshallerError: ToEntityMarshaller[ErrorResponse]): Route

  def reservationReservationIdDelete204: Route =
    complete((204, "OK"))

  def reservationReservationIdDelete422(responseError: ErrorResponse)(implicit toEntityMarshallerError: ToEntityMarshaller[ErrorResponse]): Route =
    complete((422, responseError))

  /**
   * Code: 204, Message: OK
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def reservationReservationIdDelete(reservationId: Int)
                                    (implicit toEntityMarshallerError: ToEntityMarshaller[ErrorResponse]): Route


  def reservationReservationIdGet200(responseReservation: Reservation)(implicit toEntityMarshallerReservation: ToEntityMarshaller[Reservation]): Route =
    complete((200, responseReservation))

  def reservationReservationIdGet404(responseError: ErrorResponse)(implicit toEntityMarshallerError: ToEntityMarshaller[ErrorResponse]): Route =
    complete((404, responseError))

  def reservationReservationIdGet422(responseError: ErrorResponse)(implicit toEntityMarshallerError: ToEntityMarshaller[ErrorResponse]): Route =
    complete((422, responseError))

  /**
   * Code: 200, Message: a reservation object, DataType: Reservation
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def reservationReservationIdGet(reservationId: Int)
                                 (implicit toEntityMarshallerReservation: ToEntityMarshaller[Reservation], toEntityMarshallerError: ToEntityMarshaller[ErrorResponse]): Route

  def reservationReservationIdSettlePut200(response: SettleReservationResponse)(implicit toEntityMarshallerSettleReservationResponse: ToEntityMarshaller[SettleReservationResponse]): Route =
    complete((200, response))

  def reservationReservationIdSettlePut400(responseError: ErrorResponse)(implicit toEntityMarshallerError: ToEntityMarshaller[ErrorResponse]): Route =
    complete((400, responseError))

  def reservationReservationIdSettlePut422(responseError: ErrorResponse)(implicit toEntityMarshallerError: ToEntityMarshaller[ErrorResponse]): Route =
    complete((422, responseError))

  /**
   * Code: 204, Message: a SettleReservationResponse
   * Code: 400, Message: Bad Request, DataType: Error
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def reservationReservationIdSettlePut(body: Payment, reservationId: Int)
                                       (implicit toEntityMarshallerBody: ToEntityMarshaller[Payment],
                                        toEntityMarshallerSettleReservationResponse: ToEntityMarshaller[SettleReservationResponse]
                                        , toEntityMarshallerError: ToEntityMarshaller[ErrorResponse]
                                       ): Route

}

trait DefaultApiMarshaller {

  implicit def reservationFormat: RootJsonFormat[ReservationBody]

  implicit def fromRequestUnmarshallerPayment: RootJsonFormat[Payment]

  implicit def toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]]

  implicit def toEntityMarshallerPricingPlanarray: ToEntityMarshaller[List[PricingPlan]]

  implicit def toEntityMarshallerReservationarray: ToEntityMarshaller[List[Reservation]]

  implicit def toEntityMarshallerReservation: ToEntityMarshaller[Reservation]

  implicit def toEntityMarshallerError: ToEntityMarshaller[ErrorResponse]

  implicit def toEntityMarshallerPostSuccessResponse: ToEntityMarshaller[PostSuccessResponse]

  implicit def toEntityMarshallerSettleReservationResponse: ToEntityMarshaller[SettleReservationResponse]

}

