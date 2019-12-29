package io.swagger.server.model


/**
 * @param name 
 * @param price 
 * @param timeUnit 
 * @param placeTypes 
 * @param extraTimeFine 
 */
case class PricingPlan (
  name: Option[String],
  price: Double,
  timeUnit: String,
  placeTypes: List[PlaceType],
  extraTimeFine: Double
)

