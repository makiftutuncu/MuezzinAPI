package com.mehmetakiftutuncu.muezzinapi.models

import java.util.{HashMap => JHashMap, Map => JMap}

import com.mehmetakiftutuncu.muezzinapi.utilities.{Log, Logging}
import play.api.libs.json.{JsObject, Json}

import scala.io.{BufferedSource, Source}
import scala.util.control.NonFatal
import scala.util.matching.Regex

case class City(id: Int, countryId: Int, name: String) {
  def toJson: JsObject = Json.obj("name" -> name)

  def toJavaMap: JMap[String, AnyRef] = {
    val map: JMap[String, AnyRef] = new JHashMap[String, AnyRef]()

    map.put("name", name)

    map
  }
}

object City extends Logging {
  val idToNamesMap: Map[Int, String] = {
    val regex: Regex = """(\d+?),"(.+?)"""".r

    try {
      Log.debug("Parsing cities.csv to generate name map...")

      val bufferedSource: BufferedSource = Source.fromFile("conf/cities.csv")

      val map: Map[Int, String] = bufferedSource.getLines().drop(1).map {
        case regex(idString: String, name: String) =>
          idString.toInt -> name
      }.toMap

      bufferedSource.close()

      map
    } catch {
      case NonFatal(t: Throwable) =>
        Log.error("Failed to parse cities.csv to generate name map!", t)

        Map.empty[Int, String]
    }
  }
}
