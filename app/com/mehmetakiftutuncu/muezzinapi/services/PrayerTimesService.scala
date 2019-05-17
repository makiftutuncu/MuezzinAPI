package com.mehmetakiftutuncu.muezzinapi.services

import com.github.mehmetakiftutuncu.errors.{Maybe, _}
import com.google.inject.ImplementedBy
import com.mehmetakiftutuncu.muezzinapi.data.AbstractCache
import com.mehmetakiftutuncu.muezzinapi.models.{Place, PrayerTimesOfDay}
import com.mehmetakiftutuncu.muezzinapi.services.fetchers.AbstractPrayerTimesFetcherService
import com.mehmetakiftutuncu.muezzinapi.utilities.{DateFormatter, Logging}
import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[PrayerTimesService])
trait AbstractPrayerTimesService {
  def getPrayerTimes(place: Place): Future[Maybe[List[PrayerTimesOfDay]]]
}

@Singleton
class PrayerTimesService @Inject()(Cache: AbstractCache,
                                   PrayerTimesFetcherService: AbstractPrayerTimesFetcherService,
                                   DateFormatter: DateFormatter) extends AbstractPrayerTimesService with Logging {
  override def getPrayerTimes(place: Place): Future[Maybe[List[PrayerTimesOfDay]]] = {
    val log: String = s"Failed to get prayer times for ${place.toLog}!"

    val cacheKey: String = s"prayerTimes.${place.toKey}"

    Cache.get[List[PrayerTimesOfDay]](cacheKey).flatMap { prayerTimesFromCacheAsOpt: Option[List[PrayerTimesOfDay]] =>
      if (prayerTimesFromCacheAsOpt.isDefined) {
        Future.successful(Maybe(filterOutOldPrayerTimes(prayerTimesFromCacheAsOpt.get)))
      } else {
        PrayerTimesFetcherService.getPrayerTimes(place).map { maybePrayerTimes: Maybe[List[PrayerTimesOfDay]] =>
          if (maybePrayerTimes.hasErrors) {
            Maybe(maybePrayerTimes.errors)
          } else {
            val prayerTimes: List[PrayerTimesOfDay] = maybePrayerTimes.value

            if (prayerTimes.nonEmpty) {
              Cache.set[List[PrayerTimesOfDay]](cacheKey, prayerTimes)
            }

            Maybe(prayerTimes)
          }
        }
      }
    }
  }
}
