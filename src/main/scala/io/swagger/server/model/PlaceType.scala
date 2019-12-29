package io.swagger.server.model

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat


/**
 * @param name 
 * @param description 
 */
case class PlaceType (
  name: String,
  description: String
)


object PlaceType {
  implicit val PlaceTypeMarshaller: RootJsonFormat[PlaceType] = jsonFormat2((n, d) => PlaceType(n,d))
}


