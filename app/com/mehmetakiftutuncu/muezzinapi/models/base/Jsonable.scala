package com.mehmetakiftutuncu.muezzinapi.models.base

import com.mehmetakiftutuncu.muezzinapi.utilities.error.Errors
import play.api.libs.json.JsValue

/**
 * To impose Json capabilities to extending it's extending class,
 * classes extending this trait must provide toJson and fromJson implementations.
 *
 * @tparam T Type of Jsonable, a subtype of MuezzinAPIModel model
 */
trait Jsonable[T <: MuezzinAPIModel] {
  /**
   * Converts given object to Json
   *
   * @param obj Object that will be converted to Json
   *
   * @return Json representation of given object
   */
  def toJson(obj: T): JsValue

  /**
   * Tries to convert given Json to an object of current type
   *
   * @param json Json from which object will be generated
   *
   * @return Generated object or some errors
   */
  def fromJson(json: JsValue): Either[T, Errors]
}
