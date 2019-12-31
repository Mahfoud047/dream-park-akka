package io.swagger.server.utils

import java.io.{BufferedWriter, File, FileWriter}

import spray.json._

import scala.io.Source

object FileHelpers extends DefaultJsonProtocol {

  val RESERVATIONS_PATH = "data/reservations.json"

  /**
   * write a `String` to the `filename`.
   */
  def writeFile(filename: String, s: String): Unit = {
    val file = new File(filename)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(s)
    bw.close()
  }


  def readData[T](fileName: String)(formatProtocol: {val format: JsonReader[T]}): Option[T] = {

    val stream = Source.fromFile(fileName)

    try {

      val json = stream.getLines.mkString.parseJson

      val parsed = json.convertTo[T](formatProtocol.format)

      Some(parsed)

    } catch {
      case _: Throwable => None
    } finally {
      stream.close()
    }

  }

}
