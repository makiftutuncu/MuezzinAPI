package com.mehmetakiftutuncu.muezzinapi.controllers

import com.mehmetakiftutuncu.muezzinapi.extractors.prayertimes.{CityExtractor, CountryExtractor, DistrictExtractor, PrayerTimesExtractor}
import com.mehmetakiftutuncu.muezzinapi.models.Data
import com.mehmetakiftutuncu.muezzinapi.models.base.MuezzinAPIController
import com.mehmetakiftutuncu.muezzinapi.models.prayertimes.{City, Country, District, PrayerTimes}
import com.mehmetakiftutuncu.muezzinapi.utilities.Log
import com.mehmetakiftutuncu.muezzinapi.utilities.error.{Errors, SingleError}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Action

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

/**
 * Prayer times controller of the application, provides country, city, district and prayer times data
 */
object PrayerTimesController extends MuezzinAPIController {
  /**
   * Gets a list of available countries
   *
   * @return When successful, a Json like following
   *
   *         {{{
   *         {
   *         "countries": [
   *         {
   *         "id": 2,
   *         "name": "Turkey [Türkiye]",
   *         "trName": "Türkiye"
   *         },
   *         ...
   *         ]
   *         }
   *         }}}
   *
   *         and when failed, a Json like following
   *
   *         {{{
   *         {
   *         "errors": [
   *         {
   *         "name": "...",
   *         "value": "...",
   *         "details": "..."
   *         },
   *         ...
   *         ]
   *         }
   *         }}}
   */
  def getCountries = Action.async {
    def getResult(countries: List[Country]): JsValue = {
      Json.obj("countries" -> Json.toJson(countries.map(_.toJson)))
    }

    val key = "prayertimes.countries"

    val errorsOrCountries = Data.get[List[Country]](key) {
      Country.getAllFromDatabase
    }

    if (errorsOrCountries.isLeft) {
      futureErrorResponse(errorsOrCountries.left.get)
    } else {
      val countries = errorsOrCountries.right.get

      if (countries.nonEmpty) {
        futureJsonResponse(getResult(countries))
      } else {
        CountryExtractor.extractCountries() map {
          case Left(extractErrors) =>
            errorResponse(extractErrors)

          case Right(extractedCountries) =>
            val saveErrors = Data.save[List[Country]](key, extractedCountries) {
              Country.saveAllToDatabase(extractedCountries)
            }

            if (saveErrors.hasErrors) {
              errorResponse(saveErrors)
            } else {
              jsonResponse(getResult(extractedCountries))
            }
        }
      }
    }
  }

  /**
   * Gets a list of available cities for given country
   *
   * @param country Id of the country, a number
   *
   * @return When successful, a Json like following
   *
   *         {{{
   *         {
   *         "countryId": 2,
   *         "cities": [
   *         {
   *         "id": 500,
   *         "name": "Adana"
   *         },
   *         ...
   *         ]
   *         }
   *         }}}
   *
   *         and when failed, a Json like following
   *
   *         {{{
   *         {
   *         "errors": [
   *         {
   *         "name": "...",
   *         "value": "...",
   *         "details": "..."
   *         },
   *         ...
   *         ]
   *         }
   *         }}}
   */
  def getCities(country: String) = Action.async {
    def getResult(countryId: Int, cities: List[City]): JsValue = {
      Json.obj("countryId" -> countryId, "cities" -> Json.toJson(cities.map(_.toJson)))
    }

    val countryIdAsOpt = Try(country.toInt).toOption

    if (countryIdAsOpt.isEmpty) {
      Log.error(s"""Failed to get cities for country "$country", country id is invalid!""", "PrayerTimes")
      futureErrorResponse(Errors(SingleError.InvalidData.withValue(country).withDetails("Country id is invalid.")))
    } else {
      val countryId = countryIdAsOpt.get

      val key = s"prayertimes.cities.$countryId"

      val errorsOrCities = Data.get[List[City]](key) {
        City.getAllFromDatabase(countryId)
      }

      if (errorsOrCities.isLeft) {
        futureErrorResponse(errorsOrCities.left.get)
      } else {
        val cities = errorsOrCities.right.get

        if (cities.nonEmpty) {
          futureJsonResponse(getResult(countryId, cities))
        } else {
          CityExtractor.extractCities(countryId) map {
            case Left(extractErrors) =>
              errorResponse(extractErrors)

            case Right(extractedCities) =>
              val saveErrors = Data.save[List[City]](key, extractedCities) {
                City.saveAllToDatabase(extractedCities)
              }

              if (saveErrors.hasErrors) {
                errorResponse(saveErrors)
              } else {
                jsonResponse(getResult(countryId, extractedCities))
              }
          }
        }
      }
    }
  }

  /**
   * Gets a list of available districts for given city
   *
   * @param city Id of the city, a number
   *
   * @return When successful, a Json like following
   *
   *         {{{
   *         {
   *         "cityId": 540,
   *         "districts": [
   *         {
   *         "id": 9570,
   *         "name": "Urla"
   *         },
   *         ...
   *         ]
   *         }
   *         }}}
   *
   *         and when failed, a Json like following
   *
   *         {{{
   *         {
   *         "errors": [
   *         {
   *         "name": "...",
   *         "value": "...",
   *         "details": "..."
   *         },
   *         ...
   *         ]
   *         }
   *         }}}
   */
  def getDistricts(city: String) = Action.async {
    def getResult(cityId: Int, districts: List[District]): JsValue = {
      Json.obj("cityId" -> cityId, "districts" -> Json.toJson(districts.map(_.toJson)))
    }

    val cityIdAsOpt = Try(city.toInt).toOption

    if (cityIdAsOpt.isEmpty) {
      Log.error(s"""Failed to get districts for city "$city", city id is invalid!""", "PrayerTimes")
      futureErrorResponse(Errors(SingleError.InvalidData.withValue(city).withDetails("City id is invalid.")))
    } else {
      val cityId = cityIdAsOpt.get

      val key = s"prayertimes.districts.$cityId"

      val errorsOrDistricts = Data.get[List[District]](key) {
        District.getAllFromDatabase(cityId)
      }

      if (errorsOrDistricts.isLeft) {
        futureErrorResponse(errorsOrDistricts.left.get)
      } else {
        val districts = errorsOrDistricts.right.get

        if (districts.nonEmpty) {
          futureJsonResponse(getResult(cityId, districts))
        } else {
          DistrictExtractor.extractDistricts(cityId) map {
            case Left(extractErrors) =>
              errorResponse(extractErrors)

            case Right(extractedDistricts) =>
              val saveErrors = Data.save[List[District]](key, extractedDistricts) {
                District.saveAllToDatabase(extractedDistricts)
              }

              if (saveErrors.hasErrors) {
                errorResponse(saveErrors)
              } else {
                jsonResponse(getResult(cityId, extractedDistricts))
              }
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
   *
   * @return When successful, a Json like following
   *
   *         {{{
   *         {
   *         "countryId": 2,
   *         "cityId": 509,
   *         "districtId": 9250,
   *         "districts": [
   *         {
   *         "dayDate": 1439683200,
   *         "fajr": 1439696040,
   *         "shuruq": 1439701980,
   *         "dhuhr": 1439727720,
   *         "asr": 1439741340,
   *         "maghrib": 1439752740,
   *         "isha": 1439758140,
   *         "qibla": 1439728080
   *         },
   *         ...
   *         ]
   *         }
   *         }}}
   *
   *         and when failed, a Json like following
   *
   *         {{{
   *         {
   *         "errors": [
   *         {
   *         "name": "...",
   *         "value": "...",
   *         "details": "..."
   *         },
   *         ...
   *         ]
   *         }
   *         }}}
   */
  def getPrayerTimes(country: String, city: String, district: String) = Action.async {
    def getResult(countryId: Int, cityId: Int, districtId: Option[Int], prayerTimesList: List[PrayerTimes]): JsValue = {
      Json.obj(
        "country"  -> countryId,
        "city"     -> cityId,
        "district" -> districtId,
        "times"    -> Json.toJson(prayerTimesList.map(_.toJson))
      )
    }

    val countryIdAsOpt = Try(country.toInt).toOption

    if (countryIdAsOpt.isEmpty) {
      Log.error(s"""Failed to get prayer times for country "$country", city "$city" and district "$district", country id is invalid!""", "PrayerTimes")
      futureErrorResponse(Errors(SingleError.InvalidData.withValue(country).withDetails("Country id is invalid.")))
    } else {
      val countryId: Int = countryIdAsOpt.get

      val cityIdAsOpt = Try(city.toInt).toOption

      if (cityIdAsOpt.isEmpty) {
        Log.error(s"""Failed to get prayer times for country "$country", city "$city" and district "$district", city id is invalid!""", "PrayerTimes")
        futureErrorResponse(Errors(SingleError.InvalidData.withValue(city).withDetails("City id is invalid.")))
      } else {
        val cityId: Int = cityIdAsOpt.get

        val districtId = Try(district.toInt).toOption

        val key = s"prayertimes.$countryId.$cityId.$districtId"

        val errorsOrPrayerTimesList = Data.get[List[PrayerTimes]](key) {
          PrayerTimes.getAllFromDatabase(countryId, cityId, districtId)
        }

        if (errorsOrPrayerTimesList.isLeft) {
          futureErrorResponse(errorsOrPrayerTimesList.left.get)
        } else {
          val prayerTimesList = errorsOrPrayerTimesList.right.get

          if (prayerTimesList.nonEmpty) {
            futureJsonResponse(getResult(countryId, cityId, districtId, prayerTimesList))
          } else {
            PrayerTimesExtractor.extractPrayerTimes(countryId, cityId, districtId) map {
              case Left(extractErrors) =>
                errorResponse(extractErrors)

              case Right(extractedPrayerTimesList) =>
                val saveErrors = Data.save[List[PrayerTimes]](key, extractedPrayerTimesList) {
                  PrayerTimes.saveAllToDatabase(extractedPrayerTimesList)
                }

                if (saveErrors.hasErrors) {
                  errorResponse(saveErrors)
                } else {
                  jsonResponse(getResult(countryId, cityId, districtId, extractedPrayerTimesList))
                }
            }
          }
        }
      }
    }
  }

  /**
   * Gets a list of prayer times for given country and city when given city has no districts available
   *
   * @param country  Id of the country, a number
   * @param city     Id of the city, a number
   *
   * @return When successful, a Json like following
   *
   *         {{{
   *         {
   *         "countryId": 166,
   *         "cityId": 9956,
   *         "districtId": null,
   *         "districts": [
   *         {
   *         "dayDate": 1439683200,
   *         "fajr": 1439696040,
   *         "shuruq": 1439701980,
   *         "dhuhr": 1439727720,
   *         "asr": 1439741340,
   *         "maghrib": 1439752740,
   *         "isha": 1439758140,
   *         "qibla": 1439728080
   *         },
   *         ...
   *         ]
   *         }
   *         }}}
   *
   *         and when failed, a Json like following
   *
   *         {{{
   *         {
   *         "errors": [
   *         {
   *         "name": "...",
   *         "value": "...",
   *         "details": "..."
   *         },
   *         ...
   *         ]
   *         }
   *         }}}
   */
  def getPrayerTimesWithoutDistrict(country: String, city: String) = getPrayerTimes(country, city, "")
}
