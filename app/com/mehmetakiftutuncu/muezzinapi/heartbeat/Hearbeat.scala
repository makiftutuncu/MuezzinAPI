package com.mehmetakiftutuncu.muezzinapi.heartbeat

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

import akka.actor.{ActorRef, ActorSystem, Cancellable, PoisonPill, Props}
import com.google.inject.ImplementedBy
import com.mehmetakiftutuncu.muezzinapi.heartbeat.HeartbeatActor.Beep
import com.mehmetakiftutuncu.muezzinapi.utilities._
import play.api.inject.ApplicationLifecycle

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

@ImplementedBy(classOf[Heartbeat])
trait AbstractHeartbeat

@Singleton
class Heartbeat @Inject()(ActorSystem: ActorSystem,
                          ApplicationLifecycle: ApplicationLifecycle,
                          Conf: AbstractConf,
                          WS: AbstractWS) extends AbstractHeartbeat with Logging {
  private val interval: FiniteDuration = Conf.getFiniteDuration("muezzinApi.heartbeat.interval", FiniteDuration(10, TimeUnit.MINUTES))

  Log.warn("Starting Heartbeat...")

  private val actor: ActorRef = ActorSystem.actorOf(Props(new HeartbeatActor(Conf, WS)), HeartbeatActor.actorName)

  private val cancellable: Cancellable = ActorSystem.scheduler.schedule(
    interval,
    interval,
    actor,
    Beep
  )

  ApplicationLifecycle.addStopHook {
    () =>
      Log.warn("Shutting down Heartbeat...")

      actor ! PoisonPill
      cancellable.cancel()
      ActorSystem.terminate()
  }
}
