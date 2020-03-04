package dev.akif.muezzinapi.prayer

import dev.akif.muezzinapi.prayer.Prayer._

final case class DailyPrayerTimes(fajr: Fajr,
                                  shuruq: Shuruq,
                                  dhuhr: Dhuhr,
                                  asr: Asr,
                                  maghrib: Maghrib,
                                  isha: Isha,
                                  qibla: Option[Qibla])
