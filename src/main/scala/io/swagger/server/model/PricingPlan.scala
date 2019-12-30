package io.swagger.server.model

import java.math.BigDecimal

/**
 * @param name 
 * @param pricePerHour 
 * @param minNumberOfHours 
 * @param placeTypeIds 
 * @param extraTimeFine 
 */
case class PricingPlan (
  name: String,
  pricePerHour: Double,
  minNumberOfHours: Int,
  placeTypeIds: List[Int],
  extraTimeFine: Double
)

