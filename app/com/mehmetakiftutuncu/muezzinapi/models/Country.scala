package com.mehmetakiftutuncu.muezzinapi.models

import java.util.{HashMap => JHashMap, Map => JMap}

import com.mehmetakiftutuncu.muezzinapi.utilities.{Log, Logging}
import play.api.libs.json.{JsObject, Json}

import scala.io.{BufferedSource, Source}
import scala.util.control.NonFatal
import scala.util.matching.Regex

case class Country(id: Int, name: String, nameTurkish: String, nameNative: String) {
  def toJson: JsObject = Json.obj(
    "name"        -> name,
    "nameTurkish" -> nameTurkish,
    "nameNative"  -> nameNative
  )

  def toJavaMap: JMap[String, AnyRef] = {
    val map: JMap[String, AnyRef] = new JHashMap[String, AnyRef]()

    map.put("name", name)
    map.put("nameTurkish", nameTurkish)
    map.put("nameNative", nameNative)

    map
  }
}

object Country extends Logging {
  val idToNamesMap: Map[Int, (String, String, String)] = {
    val regex: Regex = """(\d+?),"(.+?)","(.+?)","(.+?)"""".r

    try {
      Log.debug("Parsing countries.csv to generate name map...")

      val bufferedSource: BufferedSource = Source.fromFile("conf/countries.csv")

      val map: Map[Int, (String, String, String)] = bufferedSource.getLines().drop(1).map {
        case regex(idString: String, name: String, nameTurkish: String, nameNative: String) =>
          idString.toInt -> (name, nameTurkish, nameNative)
      }.toMap

      bufferedSource.close()

      map
    } catch {
      case NonFatal(t: Throwable) =>
        Log.error("Failed to parse countries.csv to generate name map!", t)

        Map.empty[Int, (String, String, String)]
    }
  }
}
