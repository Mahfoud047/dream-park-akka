package io.swagger.server.model

import io.swagger.server.enums.PlaceStatus
import io.swagger.server.enums.PlaceStatus._

/**
 * @param id 
 * @param zone 
 * @param status 
 * @param placeTypeId 
 */
case class Place (
  id: Int,
  zone: String,
  placeTypeId: Int,
  status: PlaceStatus = PlaceStatus.FREE
)


