package com.mehmetakiftutuncu.muezzinapi.utilities.error

import play.api.libs.json.{JsValue, Json}

/**
 * An immutable error container class with methods to add more than one errors
 *
 * @param errors A set of errors that were added
 */
case class Errors(errors: Set[ErrorBase] = Set.empty[ErrorBase]) {
  /**
   * Checks whether there is no error
   *
   * @return true if there is no error, false otherwise
   */
  def isEmpty: Boolean = errors.isEmpty

  /**
   * Checks whether there are errors
   *
   * @return true if there are errors, false otherwise
   */
  def nonEmpty: Boolean = !isEmpty

  /**
   * Checks whether there are errors
   *
   * @return true if there are errors, false otherwise
   */
  def hasErrors: Boolean = !isEmpty

  /**
   * Adds given errors to the current errors
   *
   * @param error       An error to add
   * @param otherErrors Zero or more errors to add
   *
   * @return A new instance of errors with given errors added
   */
  def addErrors(error: ErrorBase, otherErrors: ErrorBase*): Errors = copy(errors ++ Set(error) ++ otherErrors.toSet)

  /**
   * Adds errors from another Errors object
   *
   * @param otherErrors Other Errors object
   *
   * @return A new instance of errors with current errors combined with other Errors object
   */
  def ++(otherErrors: Errors): Errors = copy(errors ++ otherErrors.errors)

  /**
   * Checks whether given error exists in current errors
   *
   * @param error Error to check
   *
   * @return true if given error exists in current errors, false otherwise
   */
  def contains(error: ErrorBase): Boolean = errors.contains(error)

  /**
   * Checks whether an error that satisfies given check exists in current errors
   *
   * @param check A check function from ErrorBase to Boolean
   *
   * @return true if an error that satisfies given check exists in current errors, false otherwise
   */
  def exists(check: (ErrorBase) => Boolean): Boolean = errors.exists(check)

  /**
   * Converts these errors to Json
   *
   * @return Json representation of these errors
   */
  def toJson: JsValue = Json.toJson(errors.map(_.toJson))

  /**
   * toString implementation of errors as Json
   *
   * @return Json representation of these errors
   */
  override def toString: String = toJson.toString()
}

/**
 * Companion object to Errors
 */
object Errors {
  /**
   * Creates new instance with given errors
   *
   * @param errors Zero or more errors to add
   *
   * @return A new instance of errors with given errors added
   */
  def apply(errors: ErrorBase*): Errors = Errors(Set(errors:_*))

  /**
   * Creates empty errors
   *
   * @return Empty errors
   */
  def empty: Errors = Errors()
}

/** A base for errors */
abstract class ErrorBase(val name: String) {
  /**
   * Converts this error to Json
   *
   * @return Json representation of this error
   */
  def toJson: JsValue
}
