package com.mehmetakiftutuncu.muezzinapi.services

import com.github.mehmetakiftutuncu.errors.Maybe
import com.google.inject.ImplementedBy
import com.mehmetakiftutuncu.muezzinapi.data.AbstractCache
import com.mehmetakiftutuncu.muezzinapi.models.Country
import com.mehmetakiftutuncu.muezzinapi.services.fetchers.AbstractCountryFetcherService
import com.mehmetakiftutuncu.muezzinapi.utilities.Logging
import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[CountryService])
trait AbstractCountryService {
  def getCountries: Future[Maybe[List[Country]]]
}

@Singleton
class CountryService @Inject()(Cache: AbstractCache,
                               CountryFetcherService: AbstractCountryFetcherService) extends AbstractCountryService with Logging {
  override def getCountries: Future[Maybe[List[Country]]] = {
    val log: String = "Failed to get countries!"

    val cacheKey = "countries"

    Cache.get[List[Country]](cacheKey).flatMap { countriesFromCacheAsOpt: Option[List[Country]] =>
      if (countriesFromCacheAsOpt.isDefined) {
        Future.successful(Maybe(countriesFromCacheAsOpt.get))
      } else {
        CountryFetcherService.getCountries.map { maybeCountries: Maybe[List[Country]] =>
          if (maybeCountries.hasErrors) {
            Maybe(maybeCountries.errors)
          } else {
            val countries: List[Country] = maybeCountries.value

            if (countries.nonEmpty) {
              Cache.set[List[Country]](cacheKey, countries)
            }

            Maybe(countries)
          }
        }
      }
    }
  }
}
