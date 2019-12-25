package io.swagger.server.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import io.swagger.server.AkkaHttpHelper._
import io.swagger.server.model.Error
import io.swagger.server.model.Place
import io.swagger.server.model.PricingPlan
import io.swagger.server.model.Reservation

class DefaultApi(
    defaultService: DefaultApiService,
    defaultMarshaller: DefaultApiMarshaller
) {
  import defaultMarshaller._

  lazy val route: Route =
    path("places" / "free") { 
      get {
        
          
            
              
                
                  defaultService.placesFreeGet()
               
             
           
         
       
      }
    } ~
    path("places") { 
      get {
        
          
            
              
                
                  defaultService.placesGet()
               
             
           
         
       
      }
    } ~
    path("prices") { 
      get {
        
          
            
              
                
                  defaultService.pricesGet()
               
             
           
         
       
      }
    } ~
    path("reservation") { (idReservation) => 
      delete {
        
          
            
              
                
                  defaultService.reservationIdReservationDelete(idReservation = idReservation)
               
             
           
         
       
      }
    } ~
    path("reservation") { (idReservation) => 
      get {
        
          
            
              
                
                  defaultService.reservationIdReservationGet(idReservation = idReservation)
               
             
           
         
       
      }
    } ~
    path("reservation" / "settle") { (idReservation) => 
      put {
        
          
            
              
                
                  defaultService.reservationIdReservationSettlePut(idReservation = idReservation)
               
             
           
         
       
      }
    } ~
    path("reservation") { 
      post {
        parameters("body".as[String]) { (body) =>
          
            
              
                
                  defaultService.reservationPost(body = body)
               
             
           
         
        }
      }
    }
}

trait DefaultApiService {

  def placesFreeGet200(responsePlacearray: List[Place])(implicit toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]]): Route =
    complete((200, responsePlacearray))
  def placesFreeGet422(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((422, responseError))
  /**
   * Code: 200, Message: List of availlable places, DataType: List[Place]
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def placesFreeGet()
      (implicit toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]], toEntityMarshallerError: ToEntityMarshaller[Error]): Route

  def placesGet200(responsePlacearray: List[Place])(implicit toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]]): Route =
    complete((200, responsePlacearray))
  def placesGet422(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((422, responseError))
  /**
   * Code: 200, Message: List of places, DataType: List[Place]
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def placesGet()
      (implicit toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]], toEntityMarshallerError: ToEntityMarshaller[Error]): Route

  def pricesGet200(responsePricingPlanarray: List[PricingPlan])(implicit toEntityMarshallerPricingPlanarray: ToEntityMarshaller[List[PricingPlan]]): Route =
    complete((200, responsePricingPlanarray))
  def pricesGet422(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((422, responseError))
  /**
   * Code: 200, Message: List of possible prices, DataType: List[PricingPlan]
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def pricesGet()
      (implicit toEntityMarshallerPricingPlanarray: ToEntityMarshaller[List[PricingPlan]], toEntityMarshallerError: ToEntityMarshaller[Error]): Route

  def reservationIdReservationDelete204: Route =
    complete((204, "OK"))
  def reservationIdReservationDelete422(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((422, responseError))
  /**
   * Code: 204, Message: OK
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def reservationIdReservationDelete(idReservation: Int)
      (implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route

  def reservationIdReservationGet200(responseReservation: Reservation)(implicit toEntityMarshallerReservation: ToEntityMarshaller[Reservation]): Route =
    complete((200, responseReservation))
  def reservationIdReservationGet422(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((422, responseError))
  /**
   * Code: 200, Message: a reservation object, DataType: Reservation
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def reservationIdReservationGet(idReservation: Int)
      (implicit toEntityMarshallerReservation: ToEntityMarshaller[Reservation], toEntityMarshallerError: ToEntityMarshaller[Error]): Route

  def reservationIdReservationSettlePut204: Route =
    complete((204, "OK"))
  def reservationIdReservationSettlePut422(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((422, responseError))
  /**
   * Code: 204, Message: OK
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def reservationIdReservationSettlePut(idReservation: Int)
      (implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route

  def reservationPost201: Route =
    complete((201, "OK"))
  def reservationPost422(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((422, responseError))
  /**
   * Code: 201, Message: OK
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def reservationPost(body: String)
      (implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route

}

trait DefaultApiMarshaller {

  implicit def toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]]

  implicit def toEntityMarshallerError: ToEntityMarshaller[Error]

  implicit def toEntityMarshallerPlacearray: ToEntityMarshaller[List[Place]]

  implicit def toEntityMarshallerError: ToEntityMarshaller[Error]

  implicit def toEntityMarshallerPricingPlanarray: ToEntityMarshaller[List[PricingPlan]]

  implicit def toEntityMarshallerError: ToEntityMarshaller[Error]

  implicit def toEntityMarshallerError: ToEntityMarshaller[Error]

  implicit def toEntityMarshallerReservation: ToEntityMarshaller[Reservation]

  implicit def toEntityMarshallerError: ToEntityMarshaller[Error]

  implicit def toEntityMarshallerError: ToEntityMarshaller[Error]

  implicit def toEntityMarshallerError: ToEntityMarshaller[Error]

}

