package dev.akif.muezzinapi.translation

sealed abstract class Language(val code: String)

object Language {
  case object Native  extends Language("")
  case object Turkish extends Language("tr")
  case object English extends Language("en")
}
