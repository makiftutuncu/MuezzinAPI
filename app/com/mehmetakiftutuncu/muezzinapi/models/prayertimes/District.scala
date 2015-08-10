package com.mehmetakiftutuncu.muezzinapi.models.prayertimes

import com.mehmetakiftutuncu.muezzinapi.models.base.{Jsonable, MuezzinAPIModel}
import com.mehmetakiftutuncu.muezzinapi.utilities.Log
import com.mehmetakiftutuncu.muezzinapi.utilities.error.{Errors, SingleError}
import play.api.libs.json.{JsValue, Json}

case class District(id: Int, name: String) extends MuezzinAPIModel

object District extends Jsonable[District] {
  /**
   * Converts given object to Json
   *
   * @param district Object that will be converted to Json
   *
   * @return Json representation of given object
   */
  override def toJson(district: District): JsValue = {
    Json.obj(
      "id"   -> district.id,
      "name" -> district.name
    )
  }

  /**
   * Tries to convert given Json to an object of current type
   *
   * @param json Json from which object will be generated
   *
   * @return Generated object or some errors
   */
  override def fromJson(json: JsValue): Either[Errors, District] = {
    try {
      val id: Int      = (json \ "id").as[Int]
      val name: String = (json \ "name").as[String]

      Right(District(id, name))
    } catch {
      case t: Throwable =>
        Log.throwable(t, s"""Failed to convert "$json" to a district!""", "District")
        Left(Errors(SingleError.InvalidData.withValue(json.toString()).withDetails("Invalid district Json!")))
    }
  }
}
