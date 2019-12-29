package io.swagger.server.model

/**
 * @param id
 * @param zone 
 * @param status 
 * @param placeType 
 */
case class Place (
  id: Int,
  zone: String,
  status: Int,
  placeType: PlaceType
)

