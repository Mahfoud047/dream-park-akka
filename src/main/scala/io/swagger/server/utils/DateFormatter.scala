package io.swagger.server.utils

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object DateFormatter {
  val DATE_FORMATTER = DateTimeFormat.forPattern(("yyyy-MM-dd HH:mm"))


  def parseDate(str: String): Option[DateTime] = {
    try {
      val time = DateTime.parse(str, DATE_FORMATTER)
      Some(time)
    } catch {
      case _: Throwable => None
    }
  }


  def formatDate(dateTime: DateTime): Option[String] = {
    try {
      val timeStr = DATE_FORMATTER.print(dateTime)
      Some(timeStr)
    } catch {
      case _: Throwable => None
    }
  }


}
