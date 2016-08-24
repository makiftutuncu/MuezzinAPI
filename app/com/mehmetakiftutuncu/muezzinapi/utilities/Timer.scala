package com.mehmetakiftutuncu.muezzinapi.utilities

import java.time.{Duration, Instant}
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import java.util.function.UnaryOperator

object Timer extends Logging {
  private val map: AtomicReference[Map[String, Instant]] = new AtomicReference[Map[String, Instant]](Map.empty[String, Instant])

  def start(key: String): Instant = {
    val now: Instant = Instant.now
    val previousInstantAsOpt: Option[Instant] = map.get().get(key)

    if (previousInstantAsOpt.isDefined) {
      Log.warn(s"""There was already a timer started for key "$key". It will be overridden!""")
    }

    map.getAndUpdate(new UnaryOperator[Map[String, Instant]] {
      override def apply(m: Map[String, Instant]): Map[String, Instant] = {
        m + (key -> now)
      }
    })

    now
  }

  def stop(key: String): Duration = {
    val now: Instant = Instant.now
    val previousInstantAsOpt: Option[Instant] = map.get().get(key)

    if (previousInstantAsOpt.isEmpty) {
      Log.warn(s"""There no timer started for key "$key". It might have already been stopped!""")

      Duration.ofMillis(0)
    } else {
      map.getAndUpdate(new UnaryOperator[Map[String, Instant]] {
        override def apply(m: Map[String, Instant]): Map[String, Instant] = {
          m - key
        }
      })

      Duration.ofMillis(now.toEpochMilli - previousInstantAsOpt.get.toEpochMilli)
    }
  }

  def time[R](action: => R): (Duration, R) = {
    val key: String = UUID.randomUUID().toString

    start(key)
    val result: R = action
    val duration: Duration = stop(key)

    duration -> result
  }
}
