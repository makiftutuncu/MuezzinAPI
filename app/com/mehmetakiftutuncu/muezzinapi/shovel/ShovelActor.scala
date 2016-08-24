package com.mehmetakiftutuncu.muezzinapi.shovel

import java.time.Duration

import akka.actor.Actor
import com.github.mehmetakiftutuncu.errors.{CommonError, Errors, Maybe}
import com.google.firebase.database._
import com.mehmetakiftutuncu.muezzinapi.data.{AbstractCache, AbstractFirebaseRealtimeDatabase}
import com.mehmetakiftutuncu.muezzinapi.data.FirebaseRealtimeDatabase._
import com.mehmetakiftutuncu.muezzinapi.models.{Place, PrayerTimesOfDay}
import com.mehmetakiftutuncu.muezzinapi.services.AbstractPrayerTimesService
import com.mehmetakiftutuncu.muezzinapi.services.fetchers.AbstractPrayerTimesFetcherService
import com.mehmetakiftutuncu.muezzinapi.shovel.ShovelActor.Dig
import com.mehmetakiftutuncu.muezzinapi.utilities.{AbstractConf, Log, Logging, Timer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

class ShovelActor(Cache: AbstractCache,
                  Conf: AbstractConf,
                  FirebaseRealtimeDatabase: AbstractFirebaseRealtimeDatabase,
                  PrayerTimesFetcherService: AbstractPrayerTimesFetcherService,
                  PrayerTimesService: AbstractPrayerTimesService) extends Actor with Logging {
  override def receive: Receive = {
    case Dig =>
      Timer.start("shovel")

      val countryReference: DatabaseReference = FirebaseRealtimeDatabase.root / "prayerTimes" / "country"

      val placesPromise: Promise[List[Place]] = Promise[List[Place]]()

      val valueEventListener: ValueEventListener = new ValueEventListener {
        override def onCancelled(databaseError: DatabaseError): Unit = {
          val exception: DatabaseException = databaseError.toException
          val errors: Errors = Errors(CommonError.requestFailed.reason(exception.getMessage))

          Log.error("Shovel failed!", errors, exception)

          placesPromise.failure(exception)
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
                    List(Place(countrySnapshot.getKey.toInt, citySnapshot.getKey.toInt, None))
                  } else {
                    districtSnapshots.map {
                      districtSnapshot: DataSnapshot =>
                        Place(countrySnapshot.getKey.toInt, citySnapshot.getKey.toInt, Option(districtSnapshot.getKey.toInt))
                    }
                  }

                  placesForCity
              }
          }

          placesPromise.success(places)
        }
      }

      countryReference.addValueEventListener(valueEventListener)

      val futureResult: Future[(Errors, Int)] = placesPromise.future.flatMap {
        places: List[Place] =>
          countryReference.removeEventListener(valueEventListener)

          val fetchResultFutures: List[Future[Errors]] = places.map {
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

          Future.sequence(fetchResultFutures).map {
            fetchResults: List[Errors] =>
              val fetchResult: Errors = fetchResults.foldLeft(Errors.empty)(_ ++ _)

              fetchResult -> places.size
          }
      }

      futureResult.onComplete {
        case Success((errors: Errors, numberOfPlaces: Int)) =>
          val duration: Duration = Timer.stop("shovel")

          Log.warn(s"Shovel finished in ${duration.toMillis} ms! Prayer times are fetched for $numberOfPlaces places${if (errors.hasErrors) " with errors " + errors else ""}!")

        case Failure(t: Throwable) =>
          Log.error("Shovel failed!", t)
      }

    case m @ _ =>
      Log.error("Shovel failed!", Errors(CommonError.invalidData.reason("Received unknown message!").data(m.toString)))
  }
}

object ShovelActor {
  val actorName: String = "shovel"

  case object Dig
}
