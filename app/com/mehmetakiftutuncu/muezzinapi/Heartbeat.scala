package com.mehmetakiftutuncu.muezzinapi

import akka.actor.Actor
import com.mehmetakiftutuncu.muezzinapi.utilities.{Conf, Log, Web}

/**
 * A simple actor to make self requests to keep server awake
 */
class Heartbeat extends Actor {
  override def receive: Receive = {
    case _ =>
      Web.getForHtml(Conf.Url.self) map {
        ws =>
          Log.debug("I'm still alive!", "Heartbeat")
      } recover {
        case t: Throwable =>
          Log.throwable(t, "Heartbeat failed!", "Heartbeat")
      }
  }
}
