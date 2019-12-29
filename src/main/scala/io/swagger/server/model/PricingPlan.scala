package io.swagger.server.model

import scala.concurrent.duration.TimeUnit


/**
 * @param name
 * @param pricePerHour
 * @param minNumberOfHours
 * @param placeTypes
 * @param extraTimeFine    when exceeding the end Time of reservation, fine per hour
 */
case class PricingPlan (
  name: String,
  pricePerHour: Double,
  minNumberOfHours: Int,
  placeTypes: List[PlaceType],
  extraTimeFine: Double
)

