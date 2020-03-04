package dev.akif.muezzinapi.common

final case class Hour(value: Int) extends AnyVal

final case class Minute(value: Int) extends AnyVal

final case class Time(hour: Hour, minute: Minute)
