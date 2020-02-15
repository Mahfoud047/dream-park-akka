package io.swagger.server.utils

import java.io.{BufferedWriter, File, FileWriter}

import io.swagger.server.model.ErrorResponse
import spray.json._

import scala.io.Source

object FileHelpers extends DefaultJsonProtocol {

  val RESERVATIONS_FILE_PATH = "data/reservations.json"

  /**
   * write a `Object: T` to the `filename`.
   */
  def writeData[T](filename: String, data: T)(formatProtocol: {val format: JsonWriter[T]}): Option[Throwable] = {
    val file = new File(filename)
    val bw = new BufferedWriter(new FileWriter(file))

    try {
      val json: String = data.toJson(formatProtocol.format).toString

      bw.write(json)
      bw.close()

      None
    } catch {
      case err: Throwable => Some(err)
    } finally {
      bw.close()
    }

  }


  /**
   * write a `Object: T` to the `filename`.
   */
  def readData[T](fileName: String)(formatProtocol: {val format: JsonReader[T]}): Option[T] = {

    val stream = Source.fromFile(fileName)

    try {

      val json = stream.getLines.mkString.parseJson

      val parsed = json.convertTo[T](formatProtocol.format)

      Some(parsed)

    } catch {
      case err: Throwable => None
    } finally {
      stream.close()
    }


  }

}
