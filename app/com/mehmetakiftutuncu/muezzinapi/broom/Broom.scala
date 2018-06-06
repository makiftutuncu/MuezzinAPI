package com.mehmetakiftutuncu.muezzinapi.broom

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import javax.inject.{Inject, Singleton}
import akka.actor.{ActorRef, ActorSystem, Cancellable, PoisonPill, Props}
import com.google.inject.ImplementedBy
import com.mehmetakiftutuncu.muezzinapi.broom.BroomActor.Wipe
import com.mehmetakiftutuncu.muezzinapi.data.AbstractFirebaseRealtimeDatabase
import com.mehmetakiftutuncu.muezzinapi.utilities.{AbstractConf, DateFormatter, Log, Logging}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

@ImplementedBy(classOf[Broom])
trait AbstractBroom

@Singleton
class Broom @Inject()(ActorSystem: ActorSystem,
                      ApplicationLifecycle: ApplicationLifecycle,
                      Conf: AbstractConf,
                      DateFormatter: DateFormatter,
                      FirebaseRealtimeDatabase: AbstractFirebaseRealtimeDatabase) extends AbstractBroom with Logging {
  private val enabled: Boolean             = Conf.getBoolean("muezzinApi.broom.enabled", defaultValue = true)
  private val initialDelay: FiniteDuration = Conf.getFiniteDuration("muezzinApi.broom.initialDelay", FiniteDuration(2, TimeUnit.MINUTES))
  private val interval: FiniteDuration     = Conf.getFiniteDuration("muezzinApi.broom.interval", FiniteDuration(1, TimeUnit.DAYS))

  private val actor: ActorRef = ActorSystem.actorOf(
    Props(new BroomActor(Conf, DateFormatter, FirebaseRealtimeDatabase)),
    BroomActor.actorName
  )

  private val cancellable: Option[Cancellable] = {
    if (enabled) {
      val firstRun: LocalDateTime = LocalDateTime.now.plusSeconds(initialDelay.toSeconds).withNano(0)

      Log.warn(s"Starting Broom scheduled to $firstRun...")

      val c: Cancellable = ActorSystem.scheduler.schedule(
        initialDelay,
        interval,
        actor,
        Wipe
      )

      Option(c)
    } else {
      None
    }
  }

  ApplicationLifecycle.addStopHook {
    () =>
      actor ! PoisonPill
      cancellable.foreach {
        c: Cancellable =>
          Log.warn("Shutting down Broom...")
          c.cancel()
      }
      ActorSystem.terminate()
  }
}
