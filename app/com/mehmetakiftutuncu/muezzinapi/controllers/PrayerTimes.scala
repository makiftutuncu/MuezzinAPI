package com.mehmetakiftutuncu.muezzinapi.controllers

import com.mehmetakiftutuncu.muezzinapi.extractors.prayertimes.{CityExtractor, CountryExtractor, DistrictExtractor, PrayerTimesExtractor}
import com.mehmetakiftutuncu.muezzinapi.models.base.MuezzinAPIController
import com.mehmetakiftutuncu.muezzinapi.models.prayertimes.{City, Country, District}
import com.mehmetakiftutuncu.muezzinapi.utilities.Log
import com.mehmetakiftutuncu.muezzinapi.utilities.error.{Errors, SingleError}
import play.api.libs.json.Json
import play.api.mvc.Action

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

object PrayerTimes extends MuezzinAPIController {
  def getCountries = Action.async {
    CountryExtractor.extractCountries() map {
      case Left(errors)     => errorResponse(errors)
      case Right(countries) => jsonResponse(Json.toJson(countries.map(Country.toJson)))
    }
  }

  def getCities(country: String) = Action.async {
    val countryIdAsOpt = Try(country.toInt).toOption

    if (countryIdAsOpt.isEmpty) {
      Log.error(s"""Failed to get cities for country "$country", country id is invalid!""", "PrayerTimes")
      futureErrorResponse(Errors(SingleError.InvalidData.withValue(country).withDetails("Country id is invalid.")))
    } else {
      CityExtractor.extractCities(countryIdAsOpt.get) map {
        case Left(errors)  => errorResponse(errors)
        case Right(cities) => jsonResponse(Json.toJson(cities.map(City.toJson)))
      }
    }
  }

  def getDistricts(city: String) = Action.async {
    val cityIdAsOpt = Try(city.toInt).toOption

    if (cityIdAsOpt.isEmpty) {
      Log.error(s"""Failed to get districts for city "$city", city id is invalid!""", "PrayerTimes")
      futureErrorResponse(Errors(SingleError.InvalidData.withValue(city).withDetails("City id is invalid.")))
    } else {
      DistrictExtractor.extractCities(cityIdAsOpt.get) map {
        case Left(errors)     => errorResponse(errors)
        case Right(districts) => jsonResponse(Json.toJson(districts.map(District.toJson)))
      }
    }
  }

  def getTimes(country: String, city: String, district: String) = Action.async {
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

        val districtIdAsOpt = Try(district.toInt).toOption

        if (districtIdAsOpt.isEmpty) {
          Log.error(s"""Failed to get prayer times for country "$country", city "$city" and district "$district", district id is invalid!""", "PrayerTimes")
          futureErrorResponse(Errors(SingleError.InvalidData.withValue(district).withDetails("District id is invalid.")))
        } else {
          val districtId: Int = districtIdAsOpt.get

          PrayerTimesExtractor.extractPrayerTimes(countryId, cityId, districtId) map {
            case Left(errors)       => errorResponse(errors)
            case Right(prayerTimes) => jsonResponse(Json.obj(
              "country"  -> countryId,
              "city"     -> cityId,
              "district" -> districtId,
              "times"    -> Json.toJson(prayerTimes.map(com.mehmetakiftutuncu.muezzinapi.models.prayertimes.PrayerTimes.toJson))
            ))
          }
        }
      }
    }
  }
}
