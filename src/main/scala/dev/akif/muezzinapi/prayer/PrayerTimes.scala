package dev.akif.muezzinapi.prayer

import dev.akif.muezzinapi.common.{Date, OrderedMap}
import dev.akif.muezzinapi.location.Location.District

final case class PrayerTimes(location: District, times: OrderedMap[Date, DailyPrayerTimes])
