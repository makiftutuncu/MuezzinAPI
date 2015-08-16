package com.mehmetakiftutuncu.muezzinapi.models.base

import play.api.libs.json.JsValue

/**
 * To impose Json capabilities to extending it's extending class,
 * classes extending this trait must provide toJson implementation.
 *
 * @tparam T Type of Jsonable
 */
trait Jsonable[T] {
  /**
   * Converts this object to Json
   *
   * @return Json representation of this object
   */
  def toJson: JsValue

  override def toString: String = toJson.toString()
}
