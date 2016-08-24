package com.mehmetakiftutuncu.muezzinapi.services

import java.time.Duration
import javax.inject.{Inject, Singleton}

import com.github.mehmetakiftutuncu.errors.{CommonError, Errors, Maybe}
import com.google.firebase.database.DatabaseReference.CompletionListener
import com.google.firebase.database._
import com.google.inject.ImplementedBy
import com.mehmetakiftutuncu.muezzinapi.data.FirebaseRealtimeDatabase._
import com.mehmetakiftutuncu.muezzinapi.data.{AbstractCache, AbstractFirebaseRealtimeDatabase}
import com.mehmetakiftutuncu.muezzinapi.models.District
import com.mehmetakiftutuncu.muezzinapi.services.fetchers.AbstractDistrictFetcherService
import com.mehmetakiftutuncu.muezzinapi.utilities.{Log, Logging, Timer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.control.NonFatal

@ImplementedBy(classOf[DistrictService])
trait AbstractDistrictService {
  def getDistricts(countryId: Int, cityId: Int): Future[Maybe[List[District]]]
}

@Singleton
class DistrictService @Inject()(Cache: AbstractCache,
                                DistrictFetcherService: AbstractDistrictFetcherService,
                                FirebaseRealtimeDatabase: AbstractFirebaseRealtimeDatabase) extends AbstractDistrictService with Logging {
  override def getDistricts(countryId: Int, cityId: Int): Future[Maybe[List[District]]] = {
    val log: String = s"""Failed to get districts of country "$countryId" and city "$cityId"!"""

    val cacheKey: String = (FirebaseRealtimeDatabase.root / "countries" / countryId / "cities" / cityId / "districts").cacheKey

    val districtsFromCacheAsOpt: Option[List[District]] = Cache.get[List[District]](cacheKey)

    if (districtsFromCacheAsOpt.isDefined) {
      Future.successful(Maybe(districtsFromCacheAsOpt.get))
    } else {

      getDistrictsFromFirebase(countryId, cityId).flatMap {
        maybeDistrictsFromFirebase: Maybe[List[District]] =>
          if (maybeDistrictsFromFirebase.hasErrors) {
            Future.successful(Maybe(maybeDistrictsFromFirebase.errors))
          } else {
            val districtsFromFirebase: List[District] = maybeDistrictsFromFirebase.value

            if (districtsFromFirebase.nonEmpty) {
              Cache.set[List[District]](cacheKey, districtsFromFirebase)

              Future.successful(Maybe(districtsFromFirebase))
            } else {
              DistrictFetcherService.getDistricts(cityId).flatMap {
                maybeDistricts: Maybe[List[District]] =>
                  if (maybeDistricts.hasErrors) {
                    Future.successful(Maybe(maybeDistricts.errors))
                  } else {
                    val districts: List[District] = maybeDistricts.value

                    setDistrictsToFirebase(countryId, cityId, districts).map {
                      setErrors: Errors =>
                        if (setErrors.hasErrors) {
                          Maybe(setErrors)
                        } else {
                          Cache.set[List[District]](cacheKey, districts)

                          Maybe(districts)
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

  private def getDistrictsFromFirebase(countryId: Int, cityId: Int): Future[Maybe[List[District]]] = {
    val log: String = s"""Failed to get districts of country "$countryId" and city "$cityId" from Firebase Realtime Database!"""

    Timer.start(s"getDistrictsFromFirebase.$cityId")

    val districtsReference: DatabaseReference = FirebaseRealtimeDatabase.root / "countries" / countryId / "cities" / cityId / "districts"

    val promise: Promise[Maybe[List[District]]] = Promise[Maybe[List[District]]]()

    val valueEventListener: ValueEventListener = new ValueEventListener {
      override def onCancelled(databaseError: DatabaseError): Unit = {
        val exception: DatabaseException = databaseError.toException
        Log.error(log, Errors(CommonError.database.reason(exception.getMessage)), exception)
        promise.failure(exception)
      }

      override def onDataChange(dataSnapshot: DataSnapshot): Unit = {
        if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount == 0) {
          promise.success(Maybe(List.empty[District]))
        } else {
          import scala.collection.JavaConverters._

          try {
            val iterator: Iterator[DataSnapshot] = dataSnapshot.getChildren.iterator().asScala

            val districts: List[District] = iterator.toList.map {
              currentDataSnapshot: DataSnapshot =>
                val id: Int      = currentDataSnapshot.getKey.toInt
                val name: String = currentDataSnapshot.getValue(classOf[String])

                District(id, cityId, name)
            }

            promise.success(Maybe(districts))
          } catch {
            case NonFatal(t: Throwable) =>
              Log.error(log, Errors(CommonError.database.reason(t.getMessage)), t)

              promise.failure(t)
          }
        }
      }
    }

    districtsReference.addValueEventListener(valueEventListener)

    val futureResult: Future[Maybe[List[District]]] = promise.future

    futureResult.map {
      result: Maybe[List[District]] =>
        districtsReference.removeEventListener(valueEventListener)

        val duration: Duration = Timer.stop(s"getDistrictsFromFirebase.$cityId")

        Log.debug(s"""Got districts from Firebase for city "$cityId" in ${duration.toMillis} ms.""")

        result
    }.recoverWith {
      case _ =>
        districtsReference.removeEventListener(valueEventListener)
        futureResult
    }
  }

  private def setDistrictsToFirebase(countryId: Int, cityId: Int, districts: List[District]): Future[Errors] = {
    Timer.start(s"setDistrictsToFirebase.$cityId")

    val districtsReference: DatabaseReference = FirebaseRealtimeDatabase.root / "countries" / countryId / "cities" / cityId / "districts"

    val setErrorFutures: List[Future[Errors]] = districts.map {
      district: District =>
        val districtReference: DatabaseReference = districtsReference / district.id

        val promise: Promise[Errors] = Promise[Errors]()

        districtReference.setValue(district.name, new CompletionListener {
          override def onComplete(databaseError: DatabaseError, databaseReference: DatabaseReference): Unit = {
            val errors: Errors = if (databaseError != null) {
              Errors(CommonError.database.reason(databaseError.toException.getMessage).data(district.id.toString))
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

        val duration: Duration = Timer.stop(s"setDistrictsToFirebase.$cityId")

        Log.debug(s"""Set districts to Firebase for country "$countryId" and city "$cityId" in ${duration.toMillis} ms.""")

        errors
    }
  }
}
