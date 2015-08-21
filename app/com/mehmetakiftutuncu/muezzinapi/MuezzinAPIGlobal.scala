package com.mehmetakiftutuncu.muezzinapi

import akka.actor.{ActorSystem, Props, Scheduler}
import com.mehmetakiftutuncu.muezzinapi.tasks.{Broom, Heartbeat}
import com.mehmetakiftutuncu.muezzinapi.utilities.{Conf, Log}
import org.joda.time.DateTime
import play.api.libs.concurrent.Akka
import play.api.mvc.RequestHeader
import play.api.mvc.Results._
import play.api.{Application, GlobalSettings}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, duration}

/**
 * Global configuration object of MuezzinAPI
 */
object MuezzinAPIGlobal extends GlobalSettings {
  override def onStart(app: Application) {
    Log.warn("Starting Muezzin API...", "Global.onStart")

    val system: ActorSystem  = Akka.system(app)
    val scheduler: Scheduler = system.scheduler

    // Schedule heartbeat if it is enabled
    if (Conf.Heartbeat.enabled) {
      val heartbeat = system.actorOf(Props[Heartbeat], "heartbeat")

      Log.warn(s"Scheduling heartbeat with initial delay ${Conf.Heartbeat.initialDelay.toMinutes.toInt} minutes and ${Conf.Heartbeat.interval.toMinutes.toInt} minutes interval...", "Global.onStart")
      scheduler.schedule(Conf.Heartbeat.initialDelay, Conf.Heartbeat.interval, heartbeat, "Wake up!")
    }

    // Schedule broom if it is enabled
    if (Conf.Broom.enabled) {
      val broom = system.actorOf(Props[Broom], "broom")
      val now      = DateTime.now()
      val midnight = DateTime.now().withTime(0, 0, 0, 0)

      val interval     = Conf.Broom.interval.toDays.toInt
      val initialDelay = ((midnight.plusDays(interval).getMillis - now.getMillis) / 1000).toInt

      Log.warn(s"Scheduling broom with initial delay $initialDelay seconds and $interval days interval...", "Global.onStart")
      scheduler.schedule(FiniteDuration(initialDelay, duration.SECONDS), Conf.Broom.interval, broom, "Wipe!")
    }
  }

  override def onStop(app: Application) {
    Log.warn("Stopping Muezzin API...", "Global.onStop")

    // Kill actor system
    Akka.system(app).shutdown()
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Log.error(s"""Action not found for request "$request" with headers "${request.headers}"!""", "Global.onHandlerNotFound")

    Future.successful(NotFound("Action not found! Make sure your API call has correct path and parameters."))
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    Log.error(s"""Bad request "$request" with headers "${request.headers}" and error "$error"!""", "Global.onBadRequest")

    Future.successful(BadRequest("Bad Request! " + error))
  }

  override def onError(request: RequestHeader, t: Throwable) = {
    Log.throwable(t, s"""Unexpected error for request "$request" with headers "${request.headers}"!""", "Global.onError")

    Future.successful(InternalServerError("An unexpected error occurred! " + t.getMessage))
  }
}
