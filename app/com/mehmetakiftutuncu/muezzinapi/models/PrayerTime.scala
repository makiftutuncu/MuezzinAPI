package com.mehmetakiftutuncu.muezzinapi.models

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import play.api.libs.json.{JsObject, Json}

case class PrayerTime(time: LocalTime, prayerTimeType: PrayerTimeType) {
  def toJson: JsObject = Json.obj(prayerTimeType.toString.toLowerCase -> time.format(PrayerTime.timeFormatter))
}

object PrayerTime {
  val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)

  def apply(time: String, prayerTimeType: PrayerTimeType): PrayerTime = {
    PrayerTime(LocalTime.parse(time, timeFormatter), prayerTimeType)
  }
}
