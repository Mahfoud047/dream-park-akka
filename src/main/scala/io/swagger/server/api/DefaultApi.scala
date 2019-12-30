package io.swagger.server.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Route
import io.swagger.server.input_model.PostReservation
import io.swagger.server.model.{Error, Payment, Place, PricingPlan, Reservation}
import spray.json.RootJsonFormat

class DefaultApi(
                  defaultService: DefaultApiService,
                  defaultMarshaller: DefaultApiMarshaller
                ) extends SprayJsonSupport {

  import defaultMarshaller._

  lazy val route: Route =
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

          entity(as[PostReservation]) {
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
      }
  //  ~
  //      path("reservation" / IntNumber / "settle") { (reservationId) =>
  //        put {
  //
  //
  //          entity(as[Payment]) { body =>
  //            defaultService.reservationReservationIdSettlePut(body = body, reservationId = reservationId)
  //          }
  //
  //
  //        }
  //      }
}

trait DefaultApiService {

  def placeFreeGet200(responsePlacearray: List[Place])(implicit toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]]): Route =
    complete((200, responsePlacearray))

  def placeFreeGet422(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((422, responseError))

  /**
   * Code: 200, Message: List of available places, DataType: List[Place]
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def placeFreeGet()
                  (implicit toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]], toEntityMarshallerError: ToEntityMarshaller[Error]): Route

  def placeGet200(responsePlacearray: List[Place])(implicit toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]]): Route =
    complete((200, responsePlacearray))

  def placeGet422(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((422, responseError))

  /**
   * Code: 200, Message: List of places, DataType: List[Place]
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def placeGet()
              (implicit toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]], toEntityMarshallerError: ToEntityMarshaller[Error]): Route

  def pricingGet200(responsePricingPlanarray: List[PricingPlan])(implicit toEntityMarshallerPricingPlanarray: ToEntityMarshaller[List[PricingPlan]]): Route =
    complete((200, responsePricingPlanarray))

  def pricingGet422(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((422, responseError))

  /**
   * Code: 200, Message: OK, DataType: List[PricingPlan]
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def pricingGet()
                (implicit toEntityMarshallerPricingPlanarray: ToEntityMarshaller[List[PricingPlan]], toEntityMarshallerError: ToEntityMarshaller[Error]): Route

  def reservationGet200(responseReservationarray: List[Reservation])(implicit toEntityMarshallerReservationarray: ToEntityMarshaller[List[Reservation]]): Route =
    complete((200, responseReservationarray))

  def reservationGet422(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((422, responseError))

  /**
   * Code: 200, Message: a reservation object, DataType: List[Reservation]
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def reservationGet()
                    (implicit toEntityMarshallerReservationarray: ToEntityMarshaller[List[Reservation]], toEntityMarshallerError: ToEntityMarshaller[Error]): Route

  def reservationPost201: Route =
    complete((201, "Reservation added"))

  def reservationPost400(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((400, responseError))

  def reservationPost422(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((422, responseError))

  /**
   * Code: 201, Message: Reservation added
   * Code: 400, Message: Bad Request, DataType: Error
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def reservationPost(body: PostReservation)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route

  def reservationReservationIdDelete204: Route =
    complete((204, "OK"))

  def reservationReservationIdDelete422(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((422, responseError))

  /**
   * Code: 204, Message: OK
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def reservationReservationIdDelete(reservationId: Int)
                                    (implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route

  def reservationReservationIdGet200(responseReservation: Reservation)(implicit toEntityMarshallerReservation: ToEntityMarshaller[Reservation]): Route =
    complete((200, responseReservation))

  def reservationReservationIdGet422(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((422, responseError))

  /**
   * Code: 200, Message: a reservation object, DataType: Reservation
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def reservationReservationIdGet(reservationId: Int)
                                 (implicit toEntityMarshallerReservation: ToEntityMarshaller[Reservation], toEntityMarshallerError: ToEntityMarshaller[Error]): Route

  def reservationReservationIdSettlePut204: Route =
    complete((204, "OK"))

  def reservationReservationIdSettlePut400(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((400, responseError))

  def reservationReservationIdSettlePut422(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((422, responseError))

  /**
   * Code: 204, Message: OK
   * Code: 400, Message: Bad Request, DataType: Error
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def reservationReservationIdSettlePut(body: Payment, reservationId: Int)
                                       (implicit toEntityMarshallerBody: ToEntityMarshaller[Payment]

                                        , toEntityMarshallerError: ToEntityMarshaller[Error]
                                       ): Route

}

trait DefaultApiMarshaller {

  implicit def reservationFormat: RootJsonFormat[PostReservation]

  implicit def fromRequestUnmarshallerPayment: RootJsonFormat[Payment]

  implicit def toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]]

  implicit def toEntityMarshallerPricingPlanarray: ToEntityMarshaller[List[PricingPlan]]

  implicit def toEntityMarshallerReservationarray: ToEntityMarshaller[List[Reservation]]

  implicit def toEntityMarshallerReservation: ToEntityMarshaller[Reservation]

  implicit def toEntityMarshallerError: ToEntityMarshaller[Error]

}

