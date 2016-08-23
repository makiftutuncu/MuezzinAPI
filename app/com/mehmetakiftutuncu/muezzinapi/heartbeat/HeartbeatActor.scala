package com.mehmetakiftutuncu.muezzinapi.heartbeat

import akka.actor.Actor
import com.github.mehmetakiftutuncu.errors.{CommonError, Errors}
import com.mehmetakiftutuncu.muezzinapi.heartbeat.HeartbeatActor.Beep
import com.mehmetakiftutuncu.muezzinapi.utilities._
import play.api.libs.ws.WSResponse

import scala.concurrent.ExecutionContext.Implicits.global

class HeartbeatActor(Conf: AbstractConf, WS: AbstractWS) extends Actor with Logging {
  private val host: String = Conf.getString("muezzinApi.heartbeat.host", "")

  override def receive: Receive = {
    case Beep =>
      WS.url(host).get().map {
        wsResponse: WSResponse =>
          Log.debug("I am still alive!")
      }

    case m @ _ =>
      Log.error("Heartbeat failed!", Errors(CommonError.invalidData.reason("Received unknown message!").data(m.toString)))
  }
}

object HeartbeatActor {
  val actorName: String = "heartbeat"

  case object Beep
}
