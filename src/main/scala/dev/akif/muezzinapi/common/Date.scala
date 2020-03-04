package dev.akif.muezzinapi.common

final case class Year(value: Int) extends AnyVal

final case class Month(value: Int) extends AnyVal

final case class Day(value: Int) extends AnyVal

final case class Date(year: Year, month: Month, day: Day)
