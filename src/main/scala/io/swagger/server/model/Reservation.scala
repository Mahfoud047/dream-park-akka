package io.swagger.server.model

/**
 * @param placeId
 * @param startTime for example: ''2014-08-17T14:07''
 * @param endTime   for example: ''2014-08-17T16:07''
 * @param pricingPlanName
 * @param phoneNumber
 * @param licensePlate
 */
case class Reservation(
                        id: Int,
                        placeId: Int,
                        startTime: String,
                        endTime: String,
                        pricingPlanName: String,
                        phoneNumber: Option[Int],
                        licensePlate: Option[Int]
                      )

