package com.mehmetakiftutuncu.muezzinapi.controllers

import com.mehmetakiftutuncu.muezzinapi.extractors.prayertimes.{CityExtractor, CountryExtractor, DistrictExtractor, PrayerTimesExtractor}
import com.mehmetakiftutuncu.muezzinapi.models.Data
import com.mehmetakiftutuncu.muezzinapi.models.base.MuezzinAPIController
import com.mehmetakiftutuncu.muezzinapi.models.prayertimes.{City, Country, District, PrayerTimes}
import com.mehmetakiftutuncu.muezzinapi.utilities.Log
import com.mehmetakiftutuncu.muezzinapi.utilities.error.{Errors, SingleError}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Result, Action}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

/**
 * Prayer times controller of the application, provides country, city, district and prayer times data
 */
object PrayerTimesController extends MuezzinAPIController {
  /**
   * Gets a list of available countries
   *
   * @param force If this equals to "true" as String, cache and database will be bypassed
   *
   * @return When successful, a Json like following
   *
   * {{{
   * {
   *   "countries": [
   *     {
   *       "id": 2,
   *       "name": "Turkey",
   *       "trName": "Türkiye",
   *       "nativeName": "Türkiye",
   *     },
   *     ...
   *   ]
   * }
   * }}}
   *
   * and when failed, a Json like following
   *
   * {{{
   * {
   *   "errors": [
   *     {
   *       "name": "...",
   *       "value": "...",
   *       "details": "..."
   *     },
   *     ...
   *   ]
   * }
   * }}}
   */
  def getCountries(force: String) = Action.async {
    if (Try(force.toBoolean).getOrElse(false)) {
      extractCountries()
    } else {
      val errorsOrCountries = Data.get[List[Country]](countriesCacheKey) {
        Country.getAllFromDatabase
      }

      if (errorsOrCountries.isLeft) {
        futureErrorResponse(errorsOrCountries.left.get)
      } else {
        val countries = errorsOrCountries.right.get

        if (countries.nonEmpty) {
          futureJsonResponse(countriesResult(countries))
        } else {
          extractCountries()
        }
      }
    }
  }

  /**
   * Gets a list of available cities for given country
   *
   * @param country Id of the country, a number
   * @param force   If this equals to "true" as String, cache and database will be bypassed
   *
   * @return When successful, a Json like following
   *
   * {{{
   * {
   *   "countryId": 2,
   *   "cities": [
   *     {
   *       "id": 500,
   *       "name": "Adana"
   *     },
   *     ...
   *   ]
   * }
   * }}}
   *
   * and when failed, a Json like following
   *
   * {{{
   * {
   *   "errors": [
   *     {
   *       "name": "...",
   *       "value": "...",
   *       "details": "..."
   *     },
   *     ...
   *   ]
   * }
   * }}}
   */
  def getCities(country: String, force: String) = Action.async {
    val countryIdAsOpt = Try(country.toInt).toOption

    if (countryIdAsOpt.isEmpty) {
      Log.error(s"""Failed to get cities for country "$country", country id is invalid!""", "PrayerTimes.getCities")
      futureErrorResponse(Errors(SingleError.InvalidData.withValue(country).withDetails("Country id is invalid.")))
    } else {
      val countryId = countryIdAsOpt.get

      if (Try(force.toBoolean).getOrElse(false)) {
        extractCities(countryId)
      } else {
        val errorsOrCities = Data.get[List[City]](citiesCacheKey(countryId)) {
          City.getAllFromDatabase(countryId)
        }

        if (errorsOrCities.isLeft) {
          futureErrorResponse(errorsOrCities.left.get)
        } else {
          val cities = errorsOrCities.right.get

          if (cities.nonEmpty) {
            futureJsonResponse(citiesResult(countryId, cities))
          } else {
            extractCities(countryId)
          }
        }
      }
    }
  }

  /**
   * Gets a list of available districts for given city
   *
   * @param city  Id of the city, a number
   * @param force If this equals to "true" as String, cache and database will be bypassed
   *
   * @return When successful, a Json like following
   *
   * {{{
   * {
   *   "cityId": 540,
   *   "districts": [
   *     {
   *       "id": 9570,
   *       "name": "Urla"
   *     },
   *     ...
   *   ]
   * }
   * }}}
   *
   * and when failed, a Json like following
   *
   * {{{
   * {
   *   "errors": [
   *     {
   *       "name": "...",
   *       "value": "...",
   *       "details": "..."
   *     },
   *     ...
   *   ]
   * }
   * }}}
   */
  def getDistricts(city: String, force: String) = Action.async {
    val cityIdAsOpt = Try(city.toInt).toOption

    if (cityIdAsOpt.isEmpty) {
      Log.error(s"""Failed to get districts for city "$city", city id is invalid!""", "PrayerTimes.getDistricts")
      futureErrorResponse(Errors(SingleError.InvalidData.withValue(city).withDetails("City id is invalid.")))
    } else {
      val cityId = cityIdAsOpt.get

      if (Try(force.toBoolean).getOrElse(false)) {
        extractDistricts(cityId)
      } else {
        val errorsOrDistricts = Data.get[List[District]](districtsCacheKey(cityId)) {
          District.getAllFromDatabase(cityId)
        }

        if (errorsOrDistricts.isLeft) {
          futureErrorResponse(errorsOrDistricts.left.get)
        } else {
          val districts = errorsOrDistricts.right.get

          if (districts.nonEmpty) {
            futureJsonResponse(districtsResult(cityId, districts))
          } else {
            extractDistricts(cityId)
          }
        }
      }
    }
  }

  /**
   * Gets a list of prayer times for given country, city and district
   *
   * @param country  Id of the country, a number
   * @param city     Id of the city, a number
   * @param district Id of the district, a number
   * @param force    If this equals to "true" as String, cache and database will be bypassed
   *
   * @return When successful, a Json like following
   *
   * {{{
   * {
   *   "countryId": 2,
   *   "cityId": 509,
   *   "districtId": 9250,
   *   "times": [
   *     {
   *       "dayDate": 1439683200000,
   *       "fajr": 1439696040000,
   *       "shuruq": 1439701980000,
   *       "dhuhr": 1439727720000,
   *       "asr": 1439741340000,
   *       "maghrib": 1439752740000,
   *       "isha": 1439758140000,
   *       "qibla": 1439728080000
   *     },
   *     ...
   *   ]
   * }
   * }}}
   *
   * and when failed, a Json like following
   *
   * {{{
   * {
   *   "errors": [
   *     {
   *       "name": "...",
   *       "value": "...",
   *       "details": "..."
   *     },
   *     ...
   *   ]
   * }
   * }}}
   */
  def getPrayerTimes(country: String, city: String, district: String, force: String) = Action.async {
    val countryIdAsOpt = Try(country.toInt).toOption

    if (countryIdAsOpt.isEmpty) {
      Log.error(s"""Failed to get prayer times for country "$country", city "$city" and district "$district", country id is invalid!""", "PrayerTimes.getPrayerTimes")
      futureErrorResponse(Errors(SingleError.InvalidData.withValue(country).withDetails("Country id is invalid.")))
    } else {
      val countryId: Int = countryIdAsOpt.get

      val cityIdAsOpt = Try(city.toInt).toOption

      if (cityIdAsOpt.isEmpty) {
        Log.error(s"""Failed to get prayer times for country "$country", city "$city" and district "$district", city id is invalid!""", "PrayerTimes.getPrayerTimes")
        futureErrorResponse(Errors(SingleError.InvalidData.withValue(city).withDetails("City id is invalid.")))
      } else {
        val cityId: Int             = cityIdAsOpt.get
        val districtId: Option[Int] = Try(district.toInt).toOption

        if (Try(force.toBoolean).getOrElse(false)) {
          extractPrayerTimes(countryId, cityId, districtId)
        } else {
          val errorsOrPrayerTimesList = Data.get[List[PrayerTimes]](prayerTimesCacheKey(countryId, cityId, districtId)) {
            PrayerTimes.getAllFromDatabase(countryId, cityId, districtId)
          }

          if (errorsOrPrayerTimesList.isLeft) {
            futureErrorResponse(errorsOrPrayerTimesList.left.get)
          } else {
            val prayerTimesList = errorsOrPrayerTimesList.right.get

            if (prayerTimesList.nonEmpty) {
              futureJsonResponse(prayerTimesResult(countryId, cityId, districtId, prayerTimesList))
            } else {
              extractPrayerTimes(countryId, cityId, districtId)
            }
          }
        }
      }
    }
  }

  private def countriesResult(countries: List[Country]): JsValue = {
    Json.obj("countries" -> Json.toJson(countries.map(_.toJson)))
  }

  private def extractCountries(): Future[Result] = {
    CountryExtractor.extractCountries() map {
      case Left(extractErrors) =>
        errorResponse(extractErrors)

      case Right(extractedCountries) =>
        val saveErrors = Data.save[List[Country]](countriesCacheKey, extractedCountries) {
          Country.saveAllToDatabase(extractedCountries)
        }

        if (saveErrors.hasErrors) {
          errorResponse(saveErrors)
        } else {
          jsonResponse(countriesResult(extractedCountries))
        }
    }
  }

  private def citiesResult(countryId: Int, cities: List[City]): JsValue = {
    Json.obj("countryId" -> countryId, "cities" -> Json.toJson(cities.map(_.toJson)))
  }

  private def extractCities(countryId: Int): Future[Result] = {
    CityExtractor.extractCities(countryId) map {
      case Left(extractErrors) =>
        errorResponse(extractErrors)

      case Right(extractedCities) =>
        val saveErrors = Data.save[List[City]](citiesCacheKey(countryId), extractedCities) {
          City.saveAllToDatabase(extractedCities)
        }

        if (saveErrors.hasErrors) {
          errorResponse(saveErrors)
        } else {
          jsonResponse(citiesResult(countryId, extractedCities))
        }
    }
  }

  private def districtsResult(cityId: Int, districts: List[District]): JsValue = {
    Json.obj("cityId" -> cityId, "districts" -> Json.toJson(districts.map(_.toJson)))
  }

  private def extractDistricts(cityId: Int): Future[Result] = {
    DistrictExtractor.extractDistricts(cityId) map {
      case Left(extractErrors) =>
        errorResponse(extractErrors)

      case Right(extractedDistricts) =>
        val saveErrors = Data.save[List[District]](districtsCacheKey(cityId), extractedDistricts) {
          District.saveAllToDatabase(extractedDistricts)
        }

        if (saveErrors.hasErrors) {
          errorResponse(saveErrors)
        } else {
          jsonResponse(districtsResult(cityId, extractedDistricts))
        }
    }
  }

  private def extractPrayerTimes(countryId: Int, cityId: Int, districtId: Option[Int]): Future[Result] = {
    PrayerTimesExtractor.extractPrayerTimes(countryId, cityId, districtId) map {
      case Left(extractErrors) =>
        errorResponse(extractErrors)

      case Right(extractedPrayerTimesList) =>
        val saveErrors = Data.save[List[PrayerTimes]](prayerTimesCacheKey(countryId, cityId, districtId), extractedPrayerTimesList) {
          PrayerTimes.saveAllToDatabase(extractedPrayerTimesList)
        }

        if (saveErrors.hasErrors) {
          errorResponse(saveErrors)
        } else {
          jsonResponse(prayerTimesResult(countryId, cityId, districtId, extractedPrayerTimesList))
        }
    }
  }

  private def prayerTimesResult(countryId: Int, cityId: Int, districtId: Option[Int], prayerTimesList: List[PrayerTimes]): JsValue = {
    Json.obj(
      "country"  -> countryId,
      "city"     -> cityId,
      "district" -> districtId,
      "times"    -> Json.toJson(prayerTimesList.map(_.toJson))
    )
  }

  private def countriesCacheKey: String = "countries"

  private def citiesCacheKey(countryId: Int): String = s"cities.$countryId"

  private def districtsCacheKey(cityId: Int): String = s"districts.$cityId"

  private def prayerTimesCacheKey(countryId: Int, cityId: Int, districtId: Option[Int]): String = s"prayertimes.$countryId.$cityId.${districtId.getOrElse("None")}"
}
