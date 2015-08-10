package com.mehmetakiftutuncu.muezzinapi.utilities

object Conf {
  object Url {
    val worldPrayerTimes  = "http://www.diyanet.gov.tr/tr/PrayerTime/WorldPrayerTimes"
    val citiesWithCountry = "http://www.diyanet.gov.tr/PrayerTime/FillState?countryCode=%d"
    val districtWithCity  = "http://www.diyanet.gov.tr/PrayerTime/FillCity?itemId=%d"
  }
}
