package io.swagger.server.model

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import spray.json.DefaultJsonProtocol._
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


object PlaceType {
  implicit val PlaceTypeMarshaller: RootJsonFormat[PlaceType] = jsonFormat2((n, d) => PlaceType(n,d))
}


