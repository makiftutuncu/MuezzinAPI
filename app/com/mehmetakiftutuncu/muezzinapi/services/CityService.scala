package com.mehmetakiftutuncu.muezzinapi.services

import com.github.mehmetakiftutuncu.errors.Maybe
import com.google.inject.ImplementedBy
import com.mehmetakiftutuncu.muezzinapi.data.AbstractCache
import com.mehmetakiftutuncu.muezzinapi.models.City
import com.mehmetakiftutuncu.muezzinapi.services.fetchers.AbstractCityFetcherService
import com.mehmetakiftutuncu.muezzinapi.utilities.Logging
import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[CityService])
trait AbstractCityService {
  def getCities(countryId: Int): Future[Maybe[List[City]]]
}

@Singleton
class CityService @Inject()(Cache: AbstractCache,
                            CityFetcherService: AbstractCityFetcherService) extends AbstractCityService with Logging {
  override def getCities(countryId: Int): Future[Maybe[List[City]]] = {
    val log: String = s"""Failed to get cities of country "$countryId"!"""

    val cacheKey: String = s"countries.$countryId.cities"

    Cache.get[List[City]](cacheKey).flatMap { citiesFromCacheAsOpt: Option[List[City]] =>
      if (citiesFromCacheAsOpt.isDefined) {
        Future.successful(Maybe(citiesFromCacheAsOpt.get))
      } else {
        CityFetcherService.getCities(countryId).map { maybeCities: Maybe[List[City]] =>
          if (maybeCities.hasErrors) {
            Maybe(maybeCities.errors)
          } else {
            val cities: List[City] = maybeCities.value

            if (cities.nonEmpty) {
              Cache.set[List[City]](cacheKey, cities)
            }

            Maybe(cities)
          }
        }
      }
    }
  }
}
