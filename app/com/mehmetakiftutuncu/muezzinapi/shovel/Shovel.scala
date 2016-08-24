package com.mehmetakiftutuncu.muezzinapi.shovel

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

import akka.actor.{ActorRef, ActorSystem, Cancellable, PoisonPill, Props}
import com.google.inject.ImplementedBy
import com.mehmetakiftutuncu.muezzinapi.data.{AbstractCache, AbstractFirebaseRealtimeDatabase}
import com.mehmetakiftutuncu.muezzinapi.services.AbstractPrayerTimesService
import com.mehmetakiftutuncu.muezzinapi.services.fetchers.AbstractPrayerTimesFetcherService
import com.mehmetakiftutuncu.muezzinapi.shovel.ShovelActor.Dig
import com.mehmetakiftutuncu.muezzinapi.utilities.{AbstractConf, Log, Logging}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

@ImplementedBy(classOf[Shovel])
trait AbstractShovel

@Singleton
class Shovel @Inject()(ActorSystem: ActorSystem,
                       ApplicationLifecycle: ApplicationLifecycle,
                       Cache: AbstractCache,
                       Conf: AbstractConf,
                       FirebaseRealtimeDatabase: AbstractFirebaseRealtimeDatabase,
                       PrayerTimesFetcherService: AbstractPrayerTimesFetcherService,
                       PrayerTimesService: AbstractPrayerTimesService) extends AbstractShovel with Logging {
  private val enabled: Boolean             = Conf.getBoolean("muezzinApi.shovel.enabled", defaultValue = true)
  private val initialDelay: FiniteDuration = Conf.getFiniteDuration("muezzinApi.shovel.initialDelay", FiniteDuration(1, TimeUnit.MINUTES))
  private val interval: FiniteDuration     = Conf.getFiniteDuration("muezzinApi.shovel.interval", FiniteDuration(1, TimeUnit.DAYS))

  private val actor: ActorRef = ActorSystem.actorOf(
    Props(new ShovelActor(Cache, Conf, FirebaseRealtimeDatabase, PrayerTimesFetcherService, PrayerTimesService)),
    ShovelActor.actorName
  )

  private val cancellable: Option[Cancellable] = {
    if (enabled) {
      val firstRun: LocalDateTime = LocalDateTime.now.plusSeconds(initialDelay.toSeconds).withNano(0)

      Log.warn(s"Starting Shovel scheduled to $firstRun...")

      val c: Cancellable = ActorSystem.scheduler.schedule(
        initialDelay,
        interval,
        actor,
        Dig
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
          Log.warn("Shutting down Shovel...")
          c.cancel()
      }
      ActorSystem.terminate()
  }
}
