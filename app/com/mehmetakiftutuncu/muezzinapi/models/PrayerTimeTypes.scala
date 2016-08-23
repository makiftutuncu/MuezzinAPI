package com.mehmetakiftutuncu.muezzinapi.models

sealed trait PrayerTimeType

object PrayerTimeTypes {
  case object Fajr    extends PrayerTimeType
  case object Shuruq  extends PrayerTimeType
  case object Dhuhr   extends PrayerTimeType
  case object Asr     extends PrayerTimeType
  case object Maghrib extends PrayerTimeType
  case object Isha    extends PrayerTimeType
  case object Qibla   extends PrayerTimeType
}
