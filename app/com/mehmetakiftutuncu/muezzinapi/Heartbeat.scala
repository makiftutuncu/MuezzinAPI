package com.mehmetakiftutuncu.muezzinapi

import akka.actor.Actor
import com.mehmetakiftutuncu.muezzinapi.utilities.{Conf, Log, Web}

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * A simple actor to make self requests to keep server awake
 */
class Heartbeat extends Actor {
  override def receive: Receive = {
    case _ =>
      Log.debug("I'm still alive!", "Heartbeat")

      Web.getForHtml(Conf.Url.self) recover {
        case t: Throwable =>
          Log.throwable(t, "Heartbeat failed!", "Heartbeat")
      }
  }
}
