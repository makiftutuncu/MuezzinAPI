package com.mehmetakiftutuncu.muezzinapi.shovel

import java.time.Duration

import akka.actor.Actor
import com.github.mehmetakiftutuncu.errors.{CommonError, Errors, Maybe}
import com.google.firebase.database._
import com.mehmetakiftutuncu.muezzinapi.data.FirebaseRealtimeDatabase._
import com.mehmetakiftutuncu.muezzinapi.data.{AbstractCache, AbstractFirebaseRealtimeDatabase}
import com.mehmetakiftutuncu.muezzinapi.models.{Place, PrayerTimesOfDay}
import com.mehmetakiftutuncu.muezzinapi.services.AbstractPrayerTimesService
import com.mehmetakiftutuncu.muezzinapi.services.fetchers.AbstractPrayerTimesFetcherService
import com.mehmetakiftutuncu.muezzinapi.shovel.ShovelActor.Dig
import com.mehmetakiftutuncu.muezzinapi.utilities.{AbstractConf, Log, Logging, Timer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.control.NonFatal

class ShovelActor(Cache: AbstractCache,
                  Conf: AbstractConf,
                  FirebaseRealtimeDatabase: AbstractFirebaseRealtimeDatabase,
                  PrayerTimesFetcherService: AbstractPrayerTimesFetcherService,
                  PrayerTimesService: AbstractPrayerTimesService) extends Actor with Logging {
  override def receive: Receive = {
    case Dig =>
      Timer.start("shovel")

      collectPlacesToDig().foreach {
        maybePlacesToDig: Maybe[List[Place]] =>
          if (maybePlacesToDig.hasErrors) {
            val duration: Duration = Timer.stop("shovel")

            Log.warn(s"Shovel failed in ${duration.toMillis} ms with errors ${maybePlacesToDig.errors}!")
          } else {
            val placesToDig: List[Place] = maybePlacesToDig.value

            digPlaces(placesToDig).foreach {
              digErrors: Errors =>
                val duration: Duration = Timer.stop("shovel")

                if (digErrors.hasErrors) {
                  Log.warn(s"Shovel failed in ${duration.toMillis} ms with errors $digErrors!")
                } else {
                  Log.warn(s"Shovel finished in ${duration.toMillis} ms! Prayer times are fetched for ${placesToDig.size} places!")
                }
            }
          }
      }

    case m @ _ =>
      Log.error("Shovel failed!", Errors(CommonError.invalidData.reason("Received unknown message!").data(m.toString)))
  }

  private[shovel] def collectPlacesToDig(): Future[Maybe[List[Place]]] = {
    val countryReference: DatabaseReference = FirebaseRealtimeDatabase.root / "prayerTimes" / "country"

    val placesPromise: Promise[List[Place]] = Promise[List[Place]]()

    val valueEventListener: ValueEventListener = new ValueEventListener {
      override def onCancelled(databaseError: DatabaseError): Unit = {
        placesPromise.failure(databaseError.toException)
      }

      override def onDataChange(dataSnapshot: DataSnapshot): Unit = {
        import scala.collection.JavaConverters._

        val countrySnapshots: List[DataSnapshot] = dataSnapshot.getChildren.iterator().asScala.toList

        val places: List[Place] = countrySnapshots.flatMap {
          countrySnapshot: DataSnapshot =>
            val citySnapshots: List[DataSnapshot] = (countrySnapshot / "city").getChildren.iterator().asScala.toList

            citySnapshots.flatMap {
              citySnapshot: DataSnapshot =>
                val districtSnapshots: List[DataSnapshot] = (citySnapshot / "district").getChildren.iterator().asScala.toList

                val placesForCity: List[Place] = if (districtSnapshots.isEmpty) {
                  List(Place(countrySnapshot.getKey.toInt, Some(citySnapshot.getKey.toInt), None))
                } else {
                  districtSnapshots.map {
                    districtSnapshot: DataSnapshot =>
                      Place(countrySnapshot.getKey.toInt, Some(citySnapshot.getKey.toInt), Some(districtSnapshot.getKey.toInt))
                  }
                }

                placesForCity
            }
        }

        placesPromise.success(places)
      }
    }

    placesPromise.future.map {
      places: List[Place] =>
        countryReference.removeEventListener(valueEventListener)

        Maybe(places)
    }.recover {
      case NonFatal(t: Throwable) =>
        val errors: Errors = Errors(CommonError.database.reason(t.getMessage))
        Log.error("Shovel failed to collect places to dig from Firebase Realtime Database!", errors, t)

        Maybe(errors)
    }
  }

  private[shovel] def digPlaces(placesToDig: List[Place]): Future[Errors] = {
    val digFutureResults: List[Future[Errors]] = placesToDig.map {
      place: Place =>
        PrayerTimesFetcherService.getPrayerTimes(place).flatMap {
          maybePrayerTimes: Maybe[List[PrayerTimesOfDay]] =>
            if (maybePrayerTimes.hasErrors) {
              Future.successful(maybePrayerTimes.errors)
            } else {
              val prayerTimes: List[PrayerTimesOfDay] = maybePrayerTimes.value

              PrayerTimesService.setPrayerTimesToFirebase(place, prayerTimes).map {
                setErrors: Errors =>
                  Cache.remove(s"/prayerTimes/${place.toPath}")
                  setErrors
              }
            }
        }
    }

    Future.sequence(digFutureResults).map(_.foldLeft(Errors.empty)(_ ++ _)).recover {
      case NonFatal(t: Throwable) =>
        val errors: Errors = Errors(CommonError.database.reason(t.getMessage))
        Log.error("Shovel failed to dig places collected from Firebase Realtime Database!", errors, t)

        errors
    }
  }
}

object ShovelActor {
  val actorName: String = "shovel"

  case object Dig
}
