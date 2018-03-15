package com.mehmetakiftutuncu.muezzinapi

import java.time.LocalDate

import com.mehmetakiftutuncu.muezzinapi.models.PrayerTimesOfDay

package object services {
  def filterOutOldPrayerTimes(prayerTimes: List[PrayerTimesOfDay]): List[PrayerTimesOfDay] = {
    val today: LocalDate = LocalDate.now()

    prayerTimes.filterNot(_.date.isBefore(today))
  }
}
