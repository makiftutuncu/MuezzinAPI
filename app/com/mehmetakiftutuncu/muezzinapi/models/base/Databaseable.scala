package com.mehmetakiftutuncu.muezzinapi.models.base

import com.mehmetakiftutuncu.muezzinapi.utilities.error.Errors

/**
 * To impose database capabilities to extending it's extending class,
 * classes extending this trait must provide getAllFromDatabase and saveAllToDatabase implementations.
 *
 * @tparam T Type of Databaseable, a subtype of MuezzinAPIModel model
 */
trait Databaseable[T <: MuezzinAPIModel] {
  /**
   * Gets all items from database
   *
   * @return Some errors or a list of items
   */
  def getAllFromDatabase: Either[Errors, List[T]]

  /**
   * Saves given items to database
   *
   * @param items Items to save to database
   *
   * @return Non-empty errors if something goes wrong
   */
  def saveAllToDatabase(items: List[T]): Errors
}
