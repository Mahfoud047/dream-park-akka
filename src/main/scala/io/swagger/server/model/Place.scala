package io.swagger.server.model

/**
 * @param code 
 * @param zone 
 * @param status 
 * @param placeType 
 */
case class Place (
  code: Int,
  zone: String,
  status: Int,
  placeType: PlaceType
)

