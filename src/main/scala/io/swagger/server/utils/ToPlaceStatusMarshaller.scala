package io.swagger.server.utils

import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

/**
 * Based on the code found: https://groups.google.com/forum/#!topic/spray-user/RkIwRIXzDDc
 */
class ToPlaceStatusMarshaller[T <: scala.Enumeration](enu: T) extends RootJsonFormat[T#Value] {
  override def write(obj: T#Value): JsValue = JsString(obj.toString)

  override def read(json: JsValue): T#Value = {
    json match {
      case JsString(txt) => enu.withName(txt)
      case somethingElse => throw DeserializationException(s"Expected a value from enum $enu instead of $somethingElse")
    }
  }
}