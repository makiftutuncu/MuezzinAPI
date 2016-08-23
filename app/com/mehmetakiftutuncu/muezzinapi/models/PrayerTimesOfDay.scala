package com.mehmetakiftutuncu.muezzinapi.models

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.{Locale, HashMap => JHashMap, Map => JMap}

import play.api.libs.json.{JsObject, Json}

case class PrayerTimesOfDay(date: LocalDate,
                            fajr: PrayerTime,
                            shuruq: PrayerTime,
                            dhuhr: PrayerTime,
                            asr: PrayerTime,
                            maghrib: PrayerTime,
                            isha: PrayerTime,
                            qibla: PrayerTime) {
  def toJson: JsObject = Json.obj(
    date.format(PrayerTimesOfDay.dateFormatter) -> {
      fajr.toJson    ++
      shuruq.toJson  ++
      dhuhr.toJson   ++
      asr.toJson     ++
      maghrib.toJson ++
      isha.toJson    ++
      qibla.toJson
    }
  )

  def toJavaMap: JMap[String, AnyRef] = {
    val map: JMap[String, AnyRef] = new JHashMap[String, AnyRef]()

    map.put("fajr",    fajr.time.format(PrayerTime.timeFormatter))
    map.put("shuruq",  shuruq.time.format(PrayerTime.timeFormatter))
    map.put("dhuhr",   dhuhr.time.format(PrayerTime.timeFormatter))
    map.put("asr",     asr.time.format(PrayerTime.timeFormatter))
    map.put("maghrib", maghrib.time.format(PrayerTime.timeFormatter))
    map.put("isha",    isha.time.format(PrayerTime.timeFormatter))
    map.put("qibla",   qibla.time.format(PrayerTime.timeFormatter))

    map
  }
}

object PrayerTimesOfDay {
  val dateFormatter: DateTimeFormatter        = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
  val diyanetDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.US)

  def apply(date: String,
            fajr: String,
            shuruq: String,
            dhuhr: String,
            asr: String,
            maghrib: String,
            isha: String,
            qibla: String): PrayerTimesOfDay = {
    PrayerTimesOfDay(
      LocalDate.parse(date, dateFormatter),
      fajr,
      shuruq,
      dhuhr,
      asr,
      maghrib,
      isha,
      qibla
    )
  }

  def apply(date: LocalDate,
            fajr: String,
            shuruq: String,
            dhuhr: String,
            asr: String,
            maghrib: String,
            isha: String,
            qibla: String): PrayerTimesOfDay = {
    PrayerTimesOfDay(
      date,
      PrayerTime(fajr,    PrayerTimeTypes.Fajr),
      PrayerTime(shuruq,  PrayerTimeTypes.Shuruq),
      PrayerTime(dhuhr,   PrayerTimeTypes.Dhuhr),
      PrayerTime(asr,     PrayerTimeTypes.Asr),
      PrayerTime(maghrib, PrayerTimeTypes.Maghrib),
      PrayerTime(isha,    PrayerTimeTypes.Isha),
      PrayerTime(qibla,   PrayerTimeTypes.Qibla)
    )
  }
}
