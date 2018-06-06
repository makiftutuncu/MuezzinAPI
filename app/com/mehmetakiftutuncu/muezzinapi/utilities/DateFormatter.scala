package com.mehmetakiftutuncu.muezzinapi.utilities

import java.time.format.DateTimeFormatter
import java.util.Locale

import javax.inject.Inject

class DateFormatter @Inject()(Conf: AbstractConf) {
  private val dateFormatterPattern: String        = Conf.getString("muezzinApi.dateFormatter.format", "yyyy-MM-dd")
  private val diyanetDateFormatterPattern: String = Conf.getString("muezzinApi.dateFormatter.diyanet", "dd MMMM yyyy EEEE")

  val dateFormatter: DateTimeFormatter        = DateTimeFormatter.ofPattern(dateFormatterPattern, Locale.US)
  val diyanetDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(diyanetDateFormatterPattern, new Locale("tr", "TR"))
}
