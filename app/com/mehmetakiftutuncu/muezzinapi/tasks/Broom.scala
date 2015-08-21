package com.mehmetakiftutuncu.muezzinapi.tasks

import akka.actor.Actor
import com.mehmetakiftutuncu.muezzinapi.models.prayertimes.PrayerTimes
import com.mehmetakiftutuncu.muezzinapi.utilities.Log

/**
 * A simple actor to wipe old data, AKA "süpürge"
 */
class Broom extends Actor {
  override def receive: Receive = {
    case _ =>
      PrayerTimes.wipeOldPrayerTimes() match {
        case Right(deletedCount) =>
          Log.warn(s"""Broom just wiped $deletedCount (for approximately ${deletedCount / 31} locations) old prayer times.""", "Broom")

        case Left(_) =>
      }
  }
}
