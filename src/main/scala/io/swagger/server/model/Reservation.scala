package io.swagger.server.model

/**
 * @param id 
 * @param placeId 
 * @param startTime  for example: ''2014-08-17T14:07''
 * @param endTime  for example: ''2014-08-17T16:07''
 * @param pricingPlanName 
 * @param licensePlate 
 * @param phoneNumber
 */
case class Reservation (
  id: Int,
  placeId: Int,
  startTime: String,
  endTime: Option[String],
  pricingPlanName: String,
  licensePlate: Option[String],
  phoneNumber: Option[String]
)

