package com.mehmetakiftutuncu.muezzinapi.models.base

import play.api.libs.json.JsValue

/**
 * To impose Json capabilities to extending it's extending class,
 * classes extending this trait must provide toJson implementation.
 *
 * @tparam T Type of Jsonable, a subtype of MuezzinAPIModel model
 */
trait Jsonable[T <: MuezzinAPIModel] {
  /**
   * Converts this object to Json
   *
   * @return Json representation of this object
   */
  def toJson: JsValue

  override def toString: String = toJson.toString()
}
