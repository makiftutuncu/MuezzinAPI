package com.mehmetakiftutuncu.muezzinapi.services

import java.time.Duration
import javax.inject.{Inject, Singleton}

import com.github.mehmetakiftutuncu.errors.{CommonError, Errors, Maybe}
import com.google.firebase.database.DatabaseReference.CompletionListener
import com.google.firebase.database._
import com.google.inject.ImplementedBy
import com.mehmetakiftutuncu.muezzinapi.data.FirebaseRealtimeDatabase._
import com.mehmetakiftutuncu.muezzinapi.data.{AbstractCache, AbstractFirebaseRealtimeDatabase}
import com.mehmetakiftutuncu.muezzinapi.models.City
import com.mehmetakiftutuncu.muezzinapi.services.fetchers.AbstractCityFetcherService
import com.mehmetakiftutuncu.muezzinapi.utilities.{Log, Logging, Timer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.control.NonFatal

@ImplementedBy(classOf[CityService])
trait AbstractCityService {
  def getCities(countryId: Int): Future[Maybe[List[City]]]
}

@Singleton
class CityService @Inject()(Cache: AbstractCache,
                            CityFetcherService: AbstractCityFetcherService,
                            FirebaseRealtimeDatabase: AbstractFirebaseRealtimeDatabase) extends AbstractCityService with Logging {
  override def getCities(countryId: Int): Future[Maybe[List[City]]] = {
    val log: String = s"""Failed to get cities of country "$countryId"!"""

    val cacheKey: String = (FirebaseRealtimeDatabase.root / "countries" / countryId / "cities").cacheKey

    val citiesFromCacheAsOpt: Option[List[City]] = Cache.get[List[City]](cacheKey)

    if (citiesFromCacheAsOpt.isDefined) {
      Future.successful(Maybe(citiesFromCacheAsOpt.get))
    } else {
      getCitiesFromFirebase(countryId).flatMap {
        maybeCitiesFromFirebase: Maybe[List[City]] =>
          if (maybeCitiesFromFirebase.hasErrors) {
            Future.successful(Maybe(maybeCitiesFromFirebase.errors))
          } else {
            val citiesFromFirebase: List[City] = maybeCitiesFromFirebase.value

            if (citiesFromFirebase.nonEmpty) {
              Cache.set[List[City]](cacheKey, citiesFromFirebase)

              Future.successful(Maybe(citiesFromFirebase))
            } else {
              CityFetcherService.getCities(countryId).flatMap {
                maybeCities: Maybe[List[City]] =>
                  if (maybeCities.hasErrors) {
                    Future.successful(Maybe(maybeCities.errors))
                  } else {
                    val cities: List[City] = maybeCities.value

                    setCitiesToFirebase(countryId, cities).map {
                      setErrors: Errors =>
                        if (setErrors.hasErrors) {
                          Maybe(setErrors)
                        } else {
                          Cache.set[List[City]](cacheKey, cities)

                          Maybe(cities)
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

  private def getCitiesFromFirebase(countryId: Int): Future[Maybe[List[City]]] = {
    val log: String = s"""Failed to get cities of country "$countryId" from Firebase Realtime Database!"""

    Timer.start(s"getCitiesFromFirebase.$countryId")

    val citiesReference: DatabaseReference = FirebaseRealtimeDatabase.root / "countries" / countryId / "cities"

    val promise: Promise[Maybe[List[City]]] = Promise[Maybe[List[City]]]()

    val valueEventListener: ValueEventListener = new ValueEventListener {
      override def onCancelled(databaseError: DatabaseError): Unit = {
        val exception: DatabaseException = databaseError.toException
        Log.error(log, Errors(CommonError.database.reason(exception.getMessage)), exception)
        promise.failure(exception)
      }

      override def onDataChange(dataSnapshot: DataSnapshot): Unit = {
        if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount == 0) {
          promise.success(Maybe(List.empty[City]))
        } else {
          import scala.collection.JavaConverters._

          try {
            val iterator: Iterator[DataSnapshot] = dataSnapshot.getChildren.iterator().asScala

            val cities: List[City] = iterator.toList.map {
              currentDataSnapshot: DataSnapshot =>
                val id: Int      = currentDataSnapshot.getKey.toInt
                val name: String = (currentDataSnapshot / "name").getValue(classOf[String])

                City(id, countryId, name)
            }

            promise.success(Maybe(cities))
          } catch {
            case NonFatal(t: Throwable) =>
              Log.error(log, Errors(CommonError.database.reason(t.getMessage)), t)

              promise.failure(t)
          }
        }
      }
    }

    citiesReference.addValueEventListener(valueEventListener)

    val futureResult: Future[Maybe[List[City]]] = promise.future

    futureResult.map {
      result: Maybe[List[City]] =>
        citiesReference.removeEventListener(valueEventListener)

        val duration: Duration = Timer.stop(s"getCitiesFromFirebase.$countryId")

        Log.debug(s"""Got cities from Firebase for country "$countryId" in ${duration.toMillis} ms.""")

        result
    }.recoverWith {
      case _ =>
        citiesReference.removeEventListener(valueEventListener)
        futureResult
    }
  }

  private def setCitiesToFirebase(countryId: Int, cities: List[City]): Future[Errors] = {
    Timer.start(s"setCitiesToFirebase.$countryId")

    val citiesReference: DatabaseReference = FirebaseRealtimeDatabase.root / "countries" / countryId / "cities"

    val setErrorFutures: List[Future[Errors]] = cities.map {
      city: City =>
        val cityReference: DatabaseReference = citiesReference / city.id

        val promise: Promise[Errors] = Promise[Errors]()

        cityReference.updateChildren(city.toJavaMap, new CompletionListener {
          override def onComplete(databaseError: DatabaseError, databaseReference: DatabaseReference): Unit = {
            val errors: Errors = if (databaseError != null) {
              Errors(CommonError.database.reason(databaseError.toException.getMessage).data(city.id.toString))
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

        val duration: Duration = Timer.stop(s"setCitiesToFirebase.$countryId")

        Log.debug(s"""Set cities to Firebase for country "$countryId" in ${duration.toMillis} ms.""")

        errors
    }
  }
}
