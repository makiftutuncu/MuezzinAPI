package dev.akif.muezzinapi.prayer

import dev.akif.muezzinapi.common.Time

sealed abstract class Prayer(val index: Int, val name: String) {
  val time: Time
}

object Prayer {
  final case class Fajr(override val time: Time) extends Prayer(0, "fajr")

  final case class Shuruq(override val time: Time) extends Prayer(1, "shuruq")

  final case class Dhuhr(override val time: Time) extends Prayer(2, "dhuhr")

  final case class Asr(override val time: Time) extends Prayer(3, "asr")

  final case class Maghrib(override val time: Time) extends Prayer(4, "maghrib")

  final case class Isha(override val time: Time) extends Prayer(5, "isha")

  final case class Qibla(override val time: Time) extends Prayer(6, "qibla")

  def of(index: Int, time: Time): Option[Prayer] = indexBasedBuilderMap.get(index).map(_.apply(time))

  def of(name: String, time: Time): Option[Prayer] = nameBasedBuilderMap.get(name).map(_.apply(time))

  private val indexBasedBuilderMap: Map[Int, Time => Prayer] =
    Map(
      0 -> Fajr.apply,
      1 -> Shuruq.apply,
      2 -> Dhuhr.apply,
      3 -> Asr.apply,
      4 -> Maghrib.apply,
      5 -> Isha.apply,
      6 -> Qibla.apply
    )

  private val nameBasedBuilderMap: Map[String, Time => Prayer] =
    Map(
      "fajr"    -> Fajr.apply,
      "shuruq"  -> Shuruq.apply,
      "dhuhr"   -> Dhuhr.apply,
      "asr"     -> Asr.apply,
      "maghrib" -> Maghrib.apply,
      "isha"    -> Isha.apply,
      "qibla"   -> Qibla.apply
    )
}
