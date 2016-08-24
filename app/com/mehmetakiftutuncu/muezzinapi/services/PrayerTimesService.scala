package com.mehmetakiftutuncu.muezzinapi.services

import java.time.Duration
import javax.inject.{Inject, Singleton}

import com.github.mehmetakiftutuncu.errors.{Maybe, _}
import com.google.firebase.database.DatabaseReference.CompletionListener
import com.google.firebase.database._
import com.google.inject.ImplementedBy
import com.mehmetakiftutuncu.muezzinapi.data.FirebaseRealtimeDatabase._
import com.mehmetakiftutuncu.muezzinapi.data.{AbstractCache, AbstractFirebaseRealtimeDatabase}
import com.mehmetakiftutuncu.muezzinapi.models.{Place, PrayerTimesOfDay}
import com.mehmetakiftutuncu.muezzinapi.services.fetchers.AbstractPrayerTimesFetcherService
import com.mehmetakiftutuncu.muezzinapi.utilities.{Log, Logging, Timer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.control.NonFatal

@ImplementedBy(classOf[PrayerTimesService])
trait AbstractPrayerTimesService {
  def getPrayerTimes(place: Place): Future[Maybe[List[PrayerTimesOfDay]]]
}

@Singleton
class PrayerTimesService @Inject()(Cache: AbstractCache,
                                   FirebaseRealtimeDatabase: AbstractFirebaseRealtimeDatabase,
                                   PrayerTimesFetcherService: AbstractPrayerTimesFetcherService) extends AbstractPrayerTimesService with Logging {
  private val prayerTimesReference: DatabaseReference = FirebaseRealtimeDatabase.root / "prayerTimes"

  override def getPrayerTimes(place: Place): Future[Maybe[List[PrayerTimesOfDay]]] = {
    val log: String = s"Failed to get prayer times for ${place.toLog}!"

    val cacheKey: String = (FirebaseRealtimeDatabase.root / "prayerTimes" / place.toPath).cacheKey

    val prayerTimesFromCacheAsOpt: Option[List[PrayerTimesOfDay]] = Cache.get[List[PrayerTimesOfDay]](cacheKey)

    if (prayerTimesFromCacheAsOpt.isDefined) {
      Future.successful(Maybe(prayerTimesFromCacheAsOpt.get))
    } else {
      getPrayerTimesFromFirebase(place).flatMap {
        maybePrayerTimesFromFirebase: Maybe[List[PrayerTimesOfDay]] =>
          if (maybePrayerTimesFromFirebase.hasErrors) {
            Future.successful(Maybe(maybePrayerTimesFromFirebase.errors))
          } else {
            val prayerTimesFromFirebase: List[PrayerTimesOfDay] = maybePrayerTimesFromFirebase.value

            if (prayerTimesFromFirebase.nonEmpty) {
              Cache.set[List[PrayerTimesOfDay]](cacheKey, prayerTimesFromFirebase)

              Future.successful(Maybe(prayerTimesFromFirebase))
            } else {
              PrayerTimesFetcherService.getPrayerTimes(place).flatMap {
                maybePrayerTimes: Maybe[List[PrayerTimesOfDay]] =>
                  if (maybePrayerTimes.hasErrors) {
                    Future.successful(Maybe(maybePrayerTimes.errors))
                  } else {
                    val prayerTimes: List[PrayerTimesOfDay] = maybePrayerTimes.value

                    setPrayerTimesToFirebase(place, prayerTimes).map {
                      setErrors: Errors =>
                        if (setErrors.hasErrors) {
                          Maybe(setErrors)
                        } else {
                          Cache.set[List[PrayerTimesOfDay]](cacheKey, prayerTimes)

                          Maybe(prayerTimes)
                        }
                    }
                  }
              }
            }
          }
      }.recover {
        case NonFatal(t: Throwable) =>
          val errors: Errors = Errors(CommonError.database.reason(t.getMessage))
          Log.error(log, errors, t)
          Maybe(errors)
      }
    }
  }

  private def getPrayerTimesFromFirebase(place: Place): Future[Maybe[List[PrayerTimesOfDay]]] = {
    val log: String = s"Failed to get prayer times for ${place.toLog} from Firebase Realtime Database!"

    Timer.start(s"getPrayerTimesFromFirebase.${place.toPath}")

    val prayerTimesReference: DatabaseReference = FirebaseRealtimeDatabase.root / "prayerTimes" / place.toPath

    val promise: Promise[Maybe[List[PrayerTimesOfDay]]] = Promise[Maybe[List[PrayerTimesOfDay]]]()

    val valueEventListener: ValueEventListener = new ValueEventListener {
      override def onCancelled(databaseError: DatabaseError): Unit = {
        val exception: DatabaseException = databaseError.toException
        Log.error(log, Errors(CommonError.database.reason(exception.getMessage)), exception)
        promise.failure(exception)
      }

      override def onDataChange(dataSnapshot: DataSnapshot): Unit = {
        if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount == 0) {
          promise.success(Maybe(List.empty[PrayerTimesOfDay]))
        } else {
          import scala.collection.JavaConverters._

          try {
            val iterator: Iterator[DataSnapshot] = dataSnapshot.getChildren.iterator().asScala

            val prayerTimes: List[PrayerTimesOfDay] = iterator.toList.map {
              currentDataSnapshot: DataSnapshot =>
                val date: String    = currentDataSnapshot.getKey
                val fajr: String    = (currentDataSnapshot / "fajr").getValue(classOf[String])
                val shuruq: String  = (currentDataSnapshot / "shuruq").getValue(classOf[String])
                val dhuhr: String   = (currentDataSnapshot / "dhuhr").getValue(classOf[String])
                val asr: String     = (currentDataSnapshot / "asr").getValue(classOf[String])
                val maghrib: String = (currentDataSnapshot / "maghrib").getValue(classOf[String])
                val isha: String    = (currentDataSnapshot / "isha").getValue(classOf[String])
                val qibla: String   = (currentDataSnapshot / "qibla").getValue(classOf[String])

                PrayerTimesOfDay(date, fajr, shuruq, dhuhr, asr, maghrib, isha, qibla)
            }

            promise.success(Maybe(prayerTimes))
          } catch {
            case NonFatal(t: Throwable) =>
              Log.error(log, Errors(CommonError.database.reason(t.getMessage)), t)

              promise.failure(t)
          }
        }
      }
    }

    prayerTimesReference.addValueEventListener(valueEventListener)

    val futureResult: Future[Maybe[List[PrayerTimesOfDay]]] = promise.future

    futureResult.map {
      result: Maybe[List[PrayerTimesOfDay]] =>
        prayerTimesReference.removeEventListener(valueEventListener)

        val duration: Duration = Timer.stop(s"getPrayerTimesFromFirebase.${place.toPath}")

        Log.debug(s"""Got prayer times from Firebase for "${place.toLog}" in ${duration.toMillis} ms.""")

        result
    }.recoverWith {
      case _ =>
        prayerTimesReference.removeEventListener(valueEventListener)
        futureResult
    }
  }

  private def setPrayerTimesToFirebase(place: Place, prayerTimes: List[PrayerTimesOfDay]): Future[Errors] = {
    Timer.start(s"setPrayerTimesToFirebase.${place.toPath}")

    val prayerTimesReference: DatabaseReference = FirebaseRealtimeDatabase.root / "prayerTimes" / place.toPath

    val setErrorFutures: List[Future[Errors]] = prayerTimes.map {
      prayerTimesOfDay: PrayerTimesOfDay =>
        val key: String = prayerTimesOfDay.date.format(PrayerTimesOfDay.dateFormatter)
        val prayerTimesOfDayReference: DatabaseReference = prayerTimesReference / key

        val promise: Promise[Errors] = Promise[Errors]()

        prayerTimesOfDayReference.updateChildren(prayerTimesOfDay.toJavaMap, new CompletionListener {
          override def onComplete(databaseError: DatabaseError, databaseReference: DatabaseReference): Unit = {
            val errors: Errors = if (databaseError != null) {
              Errors(CommonError.database.reason(databaseError.toException.getMessage).data(key))
            } else {
              Errors.empty
            }

            promise.success(errors)
          }
        })

        promise.future
    }

    val futureSetErrors: Future[List[Errors]] = Future.sequence(setErrorFutures)

    futureSetErrors.map {
      setErrors: List[Errors] =>
        val errors: Errors = setErrors.foldLeft(Errors.empty)(_ ++ _)

        val duration: Duration = Timer.stop(s"setPrayerTimesToFirebase.${place.toPath}")

        Log.debug(s"""Set prayer times to Firebase for "${place.toLog}" in ${duration.toMillis} ms.""")

        errors
    }
  }
}
