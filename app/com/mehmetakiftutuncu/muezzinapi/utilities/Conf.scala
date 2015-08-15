package com.mehmetakiftutuncu.muezzinapi.utilities

import scala.concurrent.duration._

object Conf {
  /** Timeout value for WS request, 10 seconds by default */
  val wsTimeout: Int = 10000

  /** Timeout value for database, 5 seconds by default */
  val dbTimeout: Int = 5

  /** Time to live value for cache entries, 1 day by default */
  val cacheTTL: FiniteDuration = 1.day

  object Url {
    val countries   = "http://www.diyanet.gov.tr/tr/PrayerTime/WorldPrayerTimes"
    val cities      = "http://www.diyanet.gov.tr/PrayerTime/FillState?countryCode=%d"
    val districts   = "http://www.diyanet.gov.tr/PrayerTime/FillCity?itemId=%d"
    val prayerTimes = "http://www.diyanet.gov.tr/tr/PrayerTime/PrayerTimesList"
  }
}
