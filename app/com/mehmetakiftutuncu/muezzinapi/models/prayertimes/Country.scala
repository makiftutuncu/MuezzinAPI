package com.mehmetakiftutuncu.muezzinapi.models.prayertimes

import com.mehmetakiftutuncu.muezzinapi.models.base.{Jsonable, MuezzinAPIModel}
import com.mehmetakiftutuncu.muezzinapi.utilities.Log
import com.mehmetakiftutuncu.muezzinapi.utilities.error.{Errors, SingleError}
import play.api.libs.json.{JsValue, Json}

case class Country(id: Int, name: String) extends MuezzinAPIModel

object Country extends Jsonable[Country] {
  /**
   * Converts given object to Json
   *
   * @param country Object that will be converted to Json
   *
   * @return Json representation of given object
   */
  override def toJson(country: Country): JsValue = {
    Json.obj(
      "id"   -> country.id,
      "name" -> country.name
    )
  }

  /**
   * Tries to convert given Json to an object of current type
   *
   * @param json Json from which object will be generated
   *
   * @return Generated object or some errors
   */
  override def fromJson(json: JsValue): Either[Errors, Country] = {
    try {
      val id: Int      = (json \ "id").as[Int]
      val name: String = (json \ "name").as[String]

      Right(Country(id, name))
    } catch {
      case t: Throwable =>
        Log.throwable(t, s"""Failed to convert "$json" to a country!""", "Country")
        Left(Errors(SingleError.InvalidData.withValue(json.toString()).withDetails("Invalid country Json!")))
    }
  }
}
