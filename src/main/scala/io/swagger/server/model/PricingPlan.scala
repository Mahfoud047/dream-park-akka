package io.swagger.server.model

import java.math.BigDecimal

/**
 * @param name 
 * @param price 
 * @param timeUnit 
 * @param placeTypes 
 * @param extraTimeFine 
 */
case class PricingPlan (
  name: Option[String],
  price: BigDecimal,
  timeUnit: String,
  placeTypes: List[PlaceType],
  extraTimeFine: BigDecimal
)

