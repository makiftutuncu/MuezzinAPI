package com.mehmetakiftutuncu.muezzinapi.services

import javax.inject.{Inject, Singleton}

import com.github.mehmetakiftutuncu.errors.{CommonError, Errors, Maybe}
import com.google.firebase.database.DatabaseReference.CompletionListener
import com.google.firebase.database._
import com.google.inject.ImplementedBy
import com.mehmetakiftutuncu.muezzinapi.data.FirebaseRealtimeDatabase._
import com.mehmetakiftutuncu.muezzinapi.data.{AbstractCache, AbstractFirebaseRealtimeDatabase}
import com.mehmetakiftutuncu.muezzinapi.models.Country
import com.mehmetakiftutuncu.muezzinapi.services.fetchers.AbstractCountryFetcherService
import com.mehmetakiftutuncu.muezzinapi.utilities.{Log, Logging}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.control.NonFatal

@ImplementedBy(classOf[CountryService])
trait AbstractCountryService {
  def getCountries: Future[Maybe[List[Country]]]
}

@Singleton
class CountryService @Inject()(Cache: AbstractCache,
                               CountryFetcherService: AbstractCountryFetcherService,
                               FirebaseRealtimeDatabase: AbstractFirebaseRealtimeDatabase) extends AbstractCountryService with Logging {
  private val countriesReference: DatabaseReference = FirebaseRealtimeDatabase.root / "countries"

  override def getCountries: Future[Maybe[List[Country]]] = {
    val log: String = "Failed to get countries!"

    val countriesFromCacheAsOpt: Option[List[Country]] = Cache.get[List[Country]](countriesReference.cacheKey)

    if (countriesFromCacheAsOpt.isDefined) {
      Future.successful(Maybe(countriesFromCacheAsOpt.get))
    } else {
      getCountriesFromFirebase.flatMap {
        maybeCountriesFromFirebase: Maybe[List[Country]] =>
          if (maybeCountriesFromFirebase.hasErrors) {
            Future.successful(Maybe(maybeCountriesFromFirebase.errors))
          } else {
            val countriesFromFirebase: List[Country] = maybeCountriesFromFirebase.value

            if (countriesFromFirebase.nonEmpty) {
              Cache.set[List[Country]](countriesReference.cacheKey, countriesFromFirebase)

              Future.successful(Maybe(countriesFromFirebase))
            } else {
              CountryFetcherService.getCountries.flatMap {
                maybeCountries: Maybe[List[Country]] =>
                  if (maybeCountries.hasErrors) {
                    Future.successful(Maybe(maybeCountries.errors))
                  } else {
                    val countries: List[Country] = maybeCountries.value

                    setCountriesToFirebase(countries).map {
                      setErrors: Errors =>
                        if (setErrors.hasErrors) {
                          Maybe(setErrors)
                        } else {
                          Cache.set[List[Country]](countriesReference.cacheKey, countries)

                          Maybe(countries)
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

  private def getCountriesFromFirebase: Future[Maybe[List[Country]]] = {
    val log: String = "Failed to get countries from Firebase Realtime Database!"

    val promise: Promise[Maybe[List[Country]]] = Promise[Maybe[List[Country]]]()

    val valueEventListener: ValueEventListener = new ValueEventListener {
      override def onCancelled(databaseError: DatabaseError): Unit = {
        val exception: DatabaseException = databaseError.toException
        Log.error(log, Errors(CommonError.database.reason(exception.getMessage)), exception)
        promise.failure(exception)
      }

      override def onDataChange(dataSnapshot: DataSnapshot): Unit = {
        if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount == 0) {
          promise.success(Maybe(List.empty[Country]))
        } else {
          import scala.collection.JavaConverters._

          try {
            val iterator: Iterator[DataSnapshot] = dataSnapshot.getChildren.iterator().asScala

            val countries: List[Country] = iterator.toList.map {
              currentDataSnapshot: DataSnapshot =>
                val id: Int             = currentDataSnapshot.getKey.toInt
                val name: String        = (currentDataSnapshot / "name").getValue(classOf[String])
                val nameNative: String  = (currentDataSnapshot / "nameNative").getValue(classOf[String])
                val nameTurkish: String = (currentDataSnapshot / "nameTurkish").getValue(classOf[String])

                Country(id, name, nameTurkish, nameNative)
            }

            promise.success(Maybe(countries))
          } catch {
            case NonFatal(t: Throwable) =>
              Log.error(log, Errors(CommonError.database.reason(t.getMessage)), t)

              promise.failure(t)
          }
        }
      }
    }

    countriesReference.addValueEventListener(valueEventListener)

    val futureResult: Future[Maybe[List[Country]]] = promise.future

    futureResult.map {
      result: Maybe[List[Country]] =>
        countriesReference.removeEventListener(valueEventListener)
        result
    }.recoverWith {
      case _ =>
        countriesReference.removeEventListener(valueEventListener)
        futureResult
    }
  }

  private def setCountriesToFirebase(countries: List[Country]): Future[Errors] = {
    val setErrorFutures: List[Future[Errors]] = countries.map {
      country: Country =>
        val countryReference: DatabaseReference = countriesReference / country.id

        val promise: Promise[Errors] = Promise[Errors]()

        countryReference.updateChildren(country.toJavaMap, new CompletionListener {
          override def onComplete(databaseError: DatabaseError, databaseReference: DatabaseReference): Unit = {
            val errors: Errors = if (databaseError != null) {
              Errors(CommonError.database.reason(databaseError.toException.getMessage).data(country.id.toString))
            } else {
              Errors.empty
            }

            promise.success(errors)
          }
        })

        promise.future
    }

    val futureSetErrors: Future[List[Errors]] = Future.sequence(setErrorFutures)

    futureSetErrors.map(_.foldLeft(Errors.empty)(_ ++ _))
  }
}
