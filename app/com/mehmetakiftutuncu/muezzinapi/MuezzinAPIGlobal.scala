package com.mehmetakiftutuncu.muezzinapi

import akka.actor.Props
import com.mehmetakiftutuncu.muezzinapi.utilities.{Conf, Log}
import play.api.libs.concurrent.Akka
import play.api.mvc.RequestHeader
import play.api.mvc.Results._
import play.api.{Application, GlobalSettings}

import scala.concurrent.Future

/**
 * Global configuration object of MuezzinAPI
 */
object MuezzinAPIGlobal extends GlobalSettings {
  override def onStart(app: Application) {
    Log.warn("Starting Muezzin API...", "Global")

    // Schedule heartbeat
    val heartbeat = Akka.system(app).actorOf(Props[Heartbeat], "heartbeat")
    Akka.system(app).scheduler.schedule(Conf.heartbeatInitialDelay, Conf.heartbeatInterval, heartbeat, "Wake up!")
  }

  override def onStop(app: Application) {
    Log.warn("Stopping Muezzin API...", "Global")

    // Kill actor system to stop heartbeat
    Akka.system(app).shutdown()
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound("Action not found! Make sure your API call has correct path and parameters."))
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful(BadRequest("Bad Request! " + error))
  }

  override def onError(request: RequestHeader, ex: Throwable) = {
    Future.successful(InternalServerError("An unexpected error occurred! " + ex.getMessage))
  }
}
