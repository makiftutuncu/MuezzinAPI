package dev.akif.muezzinapi.common

import e.scala.E

object Errors {
  val notFound: E   = E("not-found",  code = 404)
  val database: E   = E("database",   code = 500)
  val unexpected: E = E("unexpected", code = 500)
}
