package com.mehmetakiftutuncu.muezzinapi.utilities

import play.api.Play

import scala.concurrent.duration
import scala.concurrent.duration.FiniteDuration

/**
 * A utility object holding some basic configuration values
 */
object Conf {
  /** Timeout value for WS request, 10 seconds by default */
  val wsTimeout: Int = getInt("muezzin.timeout.ws", 10) * 1000

  /** Timeout value for database, 5 seconds by default */
  val dbTimeout: Int = getInt("muezzin.timeout.db", 5)

  /** Time to live value for cache entries, 24 hours by default */
  val cacheTTL: FiniteDuration = FiniteDuration(getInt("muezzin.cache.ttl", 24), duration.HOURS)

  /** Broom related configuration */
  object Broom {
    /** Switch to enable/disable broom */
    val enabled: Boolean = getBoolean("muezzin.broom.enabled", defaultValue = true)

    /** Interval for broom, to delete old data on a regular basis, 1 day by default */
    val interval: FiniteDuration = FiniteDuration(getInt("muezzin.broom.interval", 1), duration.DAYS)

    /** Strength of broom, it will keep data at most this old
      * 0 days by default meaning data will start from current day, everything older than 0 days will be deleted */
    val strength: FiniteDuration = FiniteDuration(getInt("muezzin.broom.strength", 0), duration.DAYS)
  }

  /** Heartbeat related configuration */
  object Heartbeat {
    /** Switch to enable/disable heartbeat */
    val enabled: Boolean = getBoolean("muezzin.heartbeat.enabled", defaultValue = false)

    /** Interval to heartbeat, to keep server awake by sending a request to self, 20 minutes by default */
    val interval: FiniteDuration = FiniteDuration(getInt("muezzin.heartbeat.interval", 20), duration.MINUTES)

    /** Initial delay to run heartbeat, 5 minute by default */
    val initialDelay: FiniteDuration = FiniteDuration(getInt("muezzin.heartbeat.initialDelay", 5), duration.MINUTES)
  }

  /** URL related configurations */
  object Url {
    /** Muezzin APIs own URL */
    val self = getString("muezzin.url.self", "INVALID_URL")
    /** URL to get countries */
    val countries = getString("muezzin.url.countries", "INVALID_URL")
    /** URL to get cities */
    val cities = getString("muezzin.url.cities", "INVALID_URL")
    /** URL to get districts */
    val districts = getString("muezzin.url.districts", "INVALID_URL")
    /** URL to get prayer times */
    val prayerTimes = getString("muezzin.url.prayerTimes", "INVALID_URL")
  }

  /**
   * Gets an integer from configuration
   *
   * @param key          Key of configuration
   * @param defaultValue Default value if an integer with given key is not found
   *
   * @return Integer from configuration with given key or the default value
   */
  private def getInt(key: String, defaultValue: Int): Int = {
    Play.maybeApplication.flatMap(_.configuration.getInt(key)) getOrElse {
      Log.error(s"""Failed to get integer from configuration with key "$key", check your configuration!""", "Conf.getInt")
      defaultValue
    }
  }

  /**
   * Gets a string from configuration
   *
   * @param key          Key of configuration
   * @param defaultValue Default value if a string with given key is not found
   *
   * @return String from configuration with given key or the default value
   */
  private def getString(key: String, defaultValue: String): String = {
    Play.maybeApplication.flatMap(_.configuration.getString(key)) getOrElse {
      Log.error(s"""Failed to get string from configuration with key "$key", check your configuration!""", "Conf.getString")
      defaultValue
    }
  }

  /**
   * Gets a boolean from configuration
   *
   * @param key          Key of configuration
   * @param defaultValue Default value if an boolean with given key is not found
   *
   * @return Boolean from configuration with given key or the default value
   */
  private def getBoolean(key: String, defaultValue: Boolean): Boolean = {
    Play.maybeApplication.flatMap(_.configuration.getBoolean(key)) getOrElse {
      Log.error(s"""Failed to get boolean from configuration with key "$key", check your configuration!""", "Conf.getBoolean")
      defaultValue
    }
  }
}
