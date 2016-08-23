package com.mehmetakiftutuncu.muezzinapi.broom

import java.time.{LocalDate, LocalDateTime}
import java.util.concurrent.TimeUnit

import akka.actor.Actor
import com.github.mehmetakiftutuncu.errors.{CommonError, Errors}
import com.google.firebase.database.DatabaseReference.CompletionListener
import com.google.firebase.database._
import com.mehmetakiftutuncu.muezzinapi.broom.BroomActor.Wipe
import com.mehmetakiftutuncu.muezzinapi.data.AbstractFirebaseRealtimeDatabase
import com.mehmetakiftutuncu.muezzinapi.data.FirebaseRealtimeDatabase._
import com.mehmetakiftutuncu.muezzinapi.models.PrayerTimesOfDay
import com.mehmetakiftutuncu.muezzinapi.utilities.{AbstractConf, Log, Logging}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

class BroomActor(Conf: AbstractConf,
                 FirebaseRealtimeDatabase: AbstractFirebaseRealtimeDatabase) extends Actor with Logging {
  private val log: String = "Broom failed!"
  private val effect: FiniteDuration = Conf.getFiniteDuration("muezzinApi.broom.effect", FiniteDuration(1, TimeUnit.DAYS))

  override def receive: Receive = {
    case Wipe =>
      val startDate: LocalDate = LocalDateTime.now.minusSeconds(effect.toSeconds).toLocalDate

      Log.debug(s"Wiping prayer times that are older than or equal to $startDate...")

      val countryReference: DatabaseReference = FirebaseRealtimeDatabase.root / "prayerTimes" / "country"

      val referencesPromise: Promise[List[DatabaseReference]] = Promise[List[DatabaseReference]]()

      val valueEventListener: ValueEventListener = new ValueEventListener {
        override def onCancelled(databaseError: DatabaseError): Unit = {
          val exception: DatabaseException = databaseError.toException
          val errors: Errors = Errors(CommonError.requestFailed.reason(exception.getMessage))

          Log.error(log, errors, exception)

          referencesPromise.failure(exception)
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

                        date.isBefore(startDate) || date.isEqual(startDate)
                    }
                  } else {
                    districtSnapshots.flatMap {
                      districtSnapshot: DataSnapshot =>
                        districtSnapshot.getChildren.iterator().asScala.toList.takeWhile {
                          dateSnapshot: DataSnapshot =>
                            val date: LocalDate = LocalDate.parse(dateSnapshot.getKey, PrayerTimesOfDay.dateFormatter)

                            date.isBefore(startDate) || date.isEqual(startDate)
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

      val futureResult: Future[(Errors, Int)] = referencesPromise.future.flatMap {
        references: List[DatabaseReference] =>
          countryReference.removeEventListener(valueEventListener)

          val wipeResultFutures: List[Future[Errors]] = references.map {
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

          Future.sequence(wipeResultFutures).map {
            wipeResults: List[Errors] =>
              val wipeResult: Errors = wipeResults.foldLeft(Errors.empty)(_ ++ _)

              wipeResult -> references.size
          }
      }

      futureResult.onComplete {
        case Success((errors: Errors, numberOfDeletedPrayerTimes: Int)) =>
          Log.warn(s"Broom finished! $numberOfDeletedPrayerTimes prayer times are wiped${if (errors.hasErrors) " with errors " + errors else ""}!")

        case Failure(t: Throwable) =>
          Log.error(log, t)
      }

    case m @ _ =>
      Log.error(log, Errors(CommonError.invalidData.reason("Received unknown message!").data(m.toString)))
  }
}

object BroomActor {
  val actorName: String = "broom"

  case object Wipe
}
