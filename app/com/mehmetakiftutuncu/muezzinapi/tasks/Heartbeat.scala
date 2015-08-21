package com.mehmetakiftutuncu.muezzinapi.tasks

import akka.actor.Actor
import com.mehmetakiftutuncu.muezzinapi.utilities.{Conf, Log, Web}

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * A simple actor to make self requests to keep server awake
 */
class Heartbeat extends Actor {
  override def receive: Receive = {
    case _ =>
      Web.get[Unit](Conf.Url.self) {
        response =>
          Right(Log.debug("I'm still alive!", "Heartbeat"))
      } recover {
        case t: Throwable =>
          Log.throwable(t, "Heartbeat failed!", "Heartbeat")
      }
  }
}
