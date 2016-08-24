package com.mehmetakiftutuncu.muezzinapi.broom

import java.time.{Duration, LocalDate, LocalDateTime}
import java.util.concurrent.TimeUnit

import akka.actor.Actor
import com.github.mehmetakiftutuncu.errors.{CommonError, Errors, Maybe}
import com.google.firebase.database.DatabaseReference.CompletionListener
import com.google.firebase.database._
import com.mehmetakiftutuncu.muezzinapi.broom.BroomActor.Wipe
import com.mehmetakiftutuncu.muezzinapi.data.AbstractFirebaseRealtimeDatabase
import com.mehmetakiftutuncu.muezzinapi.data.FirebaseRealtimeDatabase._
import com.mehmetakiftutuncu.muezzinapi.models.PrayerTimesOfDay
import com.mehmetakiftutuncu.muezzinapi.utilities.{AbstractConf, Log, Logging, Timer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise}
import scala.util.control.NonFatal

class BroomActor(Conf: AbstractConf,
                 FirebaseRealtimeDatabase: AbstractFirebaseRealtimeDatabase) extends Actor with Logging {
  private val effect: FiniteDuration = Conf.getFiniteDuration("muezzinApi.broom.effect", FiniteDuration(1, TimeUnit.DAYS))

  override def receive: Receive = {
    case Wipe =>
      val startDate: LocalDate = LocalDateTime.now.minusSeconds(effect.toSeconds).toLocalDate

      Timer.start("broom")

      collectReferencesToWipe(startDate).foreach {
        maybeReferencesToWipe: Maybe[List[DatabaseReference]] =>
          if (maybeReferencesToWipe.hasErrors) {
            val duration: Duration = Timer.stop("broom")

            Log.warn(s"Broom failed in ${duration.toMillis} ms with errors ${maybeReferencesToWipe.errors}!")
          } else {
            val referencesToWipe: List[DatabaseReference] = maybeReferencesToWipe.value

            wipeReferences(referencesToWipe).foreach {
              wipeErrors: Errors =>
                val duration: Duration = Timer.stop("broom")

                if (wipeErrors.hasErrors) {
                  Log.warn(s"Broom failed in ${duration.toMillis} ms with errors $wipeErrors!")
                } else {
                  Log.warn(s"Broom finished in ${duration.toMillis} ms! ${referencesToWipe.size} prayer times that are older than or equal to $startDate are wiped!")
                }
            }
          }
      }

    case m @ _ =>
      Log.error("Broom failed!", Errors(CommonError.invalidData.reason("Received unknown message!").data(m.toString)))
  }

  private[broom] def collectReferencesToWipe(startDate: LocalDate): Future[Maybe[List[DatabaseReference]]] = {
    val countryReference: DatabaseReference = FirebaseRealtimeDatabase.root / "prayerTimes" / "country"

    val referencesPromise: Promise[List[DatabaseReference]] = Promise[List[DatabaseReference]]()

    val valueEventListener: ValueEventListener = new ValueEventListener {
      override def onCancelled(databaseError: DatabaseError): Unit = {
        referencesPromise.failure(databaseError.toException)
      }

      override def onDataChange(dataSnapshot: DataSnapshot): Unit = {
        import scala.collection.JavaConverters._

        val countrySnapshots: List[DataSnapshot] = dataSnapshot.getChildren.iterator().asScala.toList

        val prayerTimeReferencesToWipe: List[DatabaseReference] = countrySnapshots.flatMap {
          countrySnapshot: DataSnapshot =>
            val citySnapshots: List[DataSnapshot] = (countrySnapshot / "city").getChildren.iterator().asScala.toList

            citySnapshots.flatMap {
              citySnapshot: DataSnapshot =>
                val districtSnapshots: List[DataSnapshot] = (citySnapshot / "district").getChildren.iterator().asScala.toList

                val prayerTimeSnapshotsToWipeForCity: List[DataSnapshot] = if (districtSnapshots.isEmpty) {
                  citySnapshot.getChildren.iterator().asScala.toList.takeWhile {
                    dateSnapshot: DataSnapshot =>
                      val date: LocalDate = LocalDate.parse(dateSnapshot.getKey, PrayerTimesOfDay.dateFormatter)

                      date.compareTo(startDate) < 1
                  }
                } else {
                  districtSnapshots.flatMap {
                    districtSnapshot: DataSnapshot =>
                      districtSnapshot.getChildren.iterator().asScala.toList.takeWhile {
                        dateSnapshot: DataSnapshot =>
                          val date: LocalDate = LocalDate.parse(dateSnapshot.getKey, PrayerTimesOfDay.dateFormatter)

                          date.compareTo(startDate) < 1
                      }
                  }
                }

                prayerTimeSnapshotsToWipeForCity.map(_.getRef)
            }
        }

        referencesPromise.success(prayerTimeReferencesToWipe)
      }
    }

    countryReference.addValueEventListener(valueEventListener)

    referencesPromise.future.map {
      references: List[DatabaseReference] =>
        countryReference.removeEventListener(valueEventListener)

        Maybe(references)
    }.recover {
      case NonFatal(t: Throwable) =>
        val errors: Errors = Errors(CommonError.database.reason(t.getMessage))
        Log.error("Broom failed to collect references to wipe from Firebase Realtime Database!", errors, t)

        Maybe(errors)
    }
  }

  private[broom] def wipeReferences(referencesToWipe: List[DatabaseReference]): Future[Errors] = {
    val wipeFutureResults: List[Future[Errors]] = referencesToWipe.map {
      prayerTimeReference: DatabaseReference =>
        val promise: Promise[Errors] = Promise[Errors]()

        prayerTimeReference.removeValue(new CompletionListener {
          override def onComplete(databaseError: DatabaseError, databaseReference: DatabaseReference): Unit = {
            val errors: Errors = if (databaseError != null) {
              Errors(CommonError.database.reason(databaseError.toException.getMessage).data(databaseReference.getKey))
            } else {
              Errors.empty
            }

            promise.success(errors)
          }
        })

        promise.future
    }

    Future.sequence(wipeFutureResults).map(_.foldLeft(Errors.empty)(_ ++ _)).recover {
      case NonFatal(t: Throwable) =>
        val errors: Errors = Errors(CommonError.database.reason(t.getMessage))
        Log.error("Broom failed to wipe collected references from Firebase Realtime Database!", errors, t)

        errors
    }
  }
}

object BroomActor {
  val actorName: String = "broom"

  case object Wipe
}
