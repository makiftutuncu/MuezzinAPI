package com.mehmetakiftutuncu.muezzinapi.utilities

import scala.concurrent.duration._

/**
 * A utility object holding some basic configuration values
 */
object Conf {
  /** Timeout value for WS request, 10 seconds by default */
  val wsTimeout: Int = 10000

  /** Timeout value for database, 5 seconds by default */
  val dbTimeout: Int = 5

  /** Time to live value for cache entries, 1 day by default */
  val cacheTTL: FiniteDuration = 1.day

  /** Interval to heartbeat, to keep server awake by sending a request to self, 5 minutes by default */
  val heartbeatInterval: FiniteDuration = 5.minutes

  /** Initial delay to run heartbeat, 1 minute by default */
  val heartbeatInitialDelay: FiniteDuration = 1.minute

  /**
   * Some URLs to make requests to
   */
  object Url {
    /** Muezzin APIs own URL for heartbeat */
    val self = "https://muezzin.herokuapp.com"
    /** URL to get countries */
    val countries = "http://www.diyanet.gov.tr/tr/PrayerTime/WorldPrayerTimes"
    /** URL to get cities */
    val cities = "http://www.diyanet.gov.tr/PrayerTime/FillState?countryCode=%d"
    /** URL to get districts */
    val districts = "http://www.diyanet.gov.tr/PrayerTime/FillCity?itemId=%d"
    /** URL to get prayer times */
    val prayerTimes = "http://www.diyanet.gov.tr/tr/PrayerTime/PrayerTimesList"
  }
}
