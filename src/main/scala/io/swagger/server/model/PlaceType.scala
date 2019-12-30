package io.swagger.server.model

import spray.json.DefaultJsonProtocol.jsonFormat3
import spray.json.RootJsonFormat

/**
 * @param id 
 * @param name 
 * @param description 
 */
case class PlaceType (
  id: Int,
  name: String,
  description: String
)


