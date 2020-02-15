package fr.mipn.parc

import io.swagger.server.model._

object ResponseTypes {

  type EitherArrayPlace = Either[ErrorResponse, List[Place]]
  type EitherArrayPricingPlan= Either[ErrorResponse, List[PricingPlan]]
  type OptionAllocatePlace = Option[ErrorResponse]
  type OptionFreePlace = Option[ErrorResponse]

  type EitherCheckPricingExists = Either[ErrorResponse, Boolean]

  type EitherPostReservation= Either[ErrorResponse, PostSuccessResponse]

  type EitherArrayReservation =  Either[ErrorResponse, List[Reservation]]

  type EitherReservation =  Either[ErrorResponse, Reservation]

  type EitherSettleReservation = Either[ErrorResponse, SettleReservationResponse]

  type OptionCalculateFee = Option[Double]

}
