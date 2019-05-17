package com.mehmetakiftutuncu.muezzinapi.services

import com.github.mehmetakiftutuncu.errors.Maybe
import com.google.inject.ImplementedBy
import com.mehmetakiftutuncu.muezzinapi.data.AbstractCache
import com.mehmetakiftutuncu.muezzinapi.models.District
import com.mehmetakiftutuncu.muezzinapi.services.fetchers.AbstractDistrictFetcherService
import com.mehmetakiftutuncu.muezzinapi.utilities.Logging
import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[DistrictService])
trait AbstractDistrictService {
  def getDistricts(countryId: Int, cityId: Int): Future[Maybe[List[District]]]
}

@Singleton
class DistrictService @Inject()(Cache: AbstractCache,
                                DistrictFetcherService: AbstractDistrictFetcherService) extends AbstractDistrictService with Logging {
  override def getDistricts(countryId: Int, cityId: Int): Future[Maybe[List[District]]] = {
    val log: String = s"""Failed to get districts of country "$countryId" and city "$cityId"!"""

    val cacheKey: String = s"countries.$countryId.cities.$cityId.districts"

    Cache.get[List[District]](cacheKey).flatMap { districtsFromCacheAsOpt: Option[List[District]] =>
      if (districtsFromCacheAsOpt.isDefined) {
        Future.successful(Maybe(districtsFromCacheAsOpt.get))
      } else {
        DistrictFetcherService.getDistricts(countryId, cityId).map { maybeDistricts: Maybe[List[District]] =>
          if (maybeDistricts.hasErrors) {
            Maybe(maybeDistricts.errors)
          } else {
            val districts: List[District] = maybeDistricts.value

            if (districts.nonEmpty) {
              Cache.set[List[District]](cacheKey, districts)
            }

            Maybe(districts)
          }
        }
      }
    }
  }
}
