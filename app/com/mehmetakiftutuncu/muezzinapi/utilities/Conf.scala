package com.mehmetakiftutuncu.muezzinapi.utilities

object Conf {
  /** Timeout value for WS request, 10 seconds by default */
  val timeout: Int = 10000

  object Url {
    val countries   = "http://www.diyanet.gov.tr/tr/PrayerTime/WorldPrayerTimes"
    val cities      = "http://www.diyanet.gov.tr/PrayerTime/FillState?countryCode=%d"
    val districts   = "http://www.diyanet.gov.tr/PrayerTime/FillCity?itemId=%d"
    val prayerTimes = "http://www.diyanet.gov.tr/tr/PrayerTime/PrayerTimesList"
  }
}
