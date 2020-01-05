package fr.mipn.parc

import io.swagger.server.model._

object ResponseTypes {
  type EitherArrayPlace = Either[Error, List[Place]]
  type EitherArrayPricingPlan= Either[Error, List[PricingPlan]]
  type OptionAllocatePlace = Option[Error]
  type OptionFreePlace = Option[Error]

  type EitherCheckPricingExists = Either[Error, Boolean]

  type EitherPostReservation= Either[Error, Nothing]
  type EitherArrayReservation = Either[Error, List[Reservation]]
}
