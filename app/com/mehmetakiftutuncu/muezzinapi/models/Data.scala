package com.mehmetakiftutuncu.muezzinapi.models

import com.mehmetakiftutuncu.muezzinapi.utilities.error.Errors
import com.mehmetakiftutuncu.muezzinapi.utilities.{Conf, Log}
import play.api.Play.current
import play.api.cache.Cache

import scala.concurrent.duration._
import scala.util.Try

/**
 * A utility object to get and set data using both cache and DB
 */
object Data {
  /**
   * Tries to gets data from cache; if not found there, from DB
   *
   * @param key     Key of the data on cache
   * @param dbBlock A function to get data from DB
   * @param ttl     Time to live value for cache, 1 day by default,
   *                This will be used to save data to cache if data is not found on cache but found on db
   *
   * @tparam T Type of the data to get
   *
   * @return Requested data or some errors
   */
  def get[T](key: String, ttl: FiniteDuration = Conf.cacheTTL)(dbBlock: => Either[Errors, T]): Either[Errors, T] = {
    Log.debug(s"""Getting from cache with key "$key"...""")

    // Check cache
    val fromCacheAsOpt: Option[T] = Cache.get(key) flatMap {
      fromCacheAsAny: Any =>
        Try(fromCacheAsAny.asInstanceOf[T]).toOption
    }

    if (fromCacheAsOpt.isDefined) {
      // Found on cache
      Right(fromCacheAsOpt.get)
    } else {
      // Not found in cache, check DB
      val dbBlockResult = dbBlock

      if (dbBlockResult.isRight) {
        Log.debug(s"""Saving to cache with key "$key" after finding it on database...""")

        Cache.set(key, dbBlockResult.right.get, ttl)
      }

      dbBlockResult
    }
  }

  /**
   * Tries to save given data to first DB, then cache
   *
   * @param key     Key of the data on cache
   * @param obj     Data to save
   * @param ttl     Time to live value for cache, 1 day by default
   * @param dbBlock A function to set data to DB
   *
   * @tparam T Type of data to save
   *
   * @return Empty errors if successful, non-empty errors otherwise
   */
  def save[T](key: String, obj: T, ttl: FiniteDuration = Conf.cacheTTL)(dbBlock: => Errors): Errors = {
    // Set to DB
    val dbErrors: Errors = dbBlock

    if (!dbErrors.hasErrors) {
      Log.debug(s"""Saving to cache with key "$key"...""")

      // Successfully saved to DB, set to cache
      Cache.set(key, obj, ttl)
    }

    dbErrors
  }
}
