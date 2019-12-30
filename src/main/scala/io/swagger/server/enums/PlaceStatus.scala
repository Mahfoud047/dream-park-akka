package io.swagger.server.enums

import io.swagger.server.utils.ToPlaceStatusMarshaller

object PlaceStatus extends Enumeration {
  type PlaceStatus = Value
  val FREE, TAKEN, UNAVAILABLE = Value


  implicit val toPlaceStatusMarshaller: ToPlaceStatusMarshaller[PlaceStatus.type] = new ToPlaceStatusMarshaller(PlaceStatus)

}
