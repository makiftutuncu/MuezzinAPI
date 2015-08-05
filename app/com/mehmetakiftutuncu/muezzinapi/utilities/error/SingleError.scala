package com.mehmetakiftutuncu.muezzinapi.utilities.error

import play.api.libs.json.{JsValue, Json}

/**
 * Represents a single error
 *
 * @param name    Name of the error
 * @param value   Value on which the error occurred, can be empty
 * @param details Details of the error, can be empty
 */
class SingleError(name: String, value: Option[String] = None, details: Option[String] = None) extends ErrorBase(name) {
  /**
   * Creates a new single error with given value
   *
   * @param newValue Value of the new single error
   *
   * @return A new single error instance with given value
   */
  def withValue(newValue: String): SingleError = new SingleError(name, Option(newValue), details)

  /**
   * Creates a new single error with given details
   *
   * @param newDetails Value of the new single error
   *
   * @return A new single error instance with given details
   */
  def withDetails(newDetails: String): SingleError = new SingleError(name, value, Option(newDetails))

  /**
   * Converts this error to Json
   *
   * @return Json representation of this error
   */
  def toJson: JsValue = {
    Json.obj(
      "name"    -> name,
      "value"   -> value,
      "details" -> details
    )
  }

  /**
   * toString implementation of error as Json
   *
   * @return Json representation of this error
   */
  override def toString: String = toJson.toString()
}
