package com.mehmetakiftutuncu.muezzinapi.extractors.prayertimes

import com.mehmetakiftutuncu.muezzinapi.models.prayertimes.City
import com.mehmetakiftutuncu.muezzinapi.utilities.error.{Errors, SingleError}
import com.mehmetakiftutuncu.muezzinapi.utilities.{Conf, Log, Utils, Web}
import play.api.libs.json.{JsArray, JsValue}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

object CityExtractor {
  def extractCities(countryId: Int): Future[Either[Errors, List[City]]] = {
    Web.getForJson(Conf.Url.cities.format(countryId)) map {
      case Left(getPageErrors) =>
        Left(getPageErrors)

      case Right(citiesPage) =>
        parseCities(countryId, citiesPage)
    }
  }

  private def parseCities(countryId: Int, page: JsValue): Either[Errors, List[City]] = {
    try {
      Log.debug(s"""Parsing cities for country "$countryId"...""", "CityExtractor")

      val citiesJsonAsOpt = page.asOpt[JsArray]

      if (citiesJsonAsOpt.isEmpty) {
        Log.error(s"""Failed to parse cities for country "$countryId". Page has invalid format: $page""", "CityExtractor")
        Left(Errors(SingleError.InvalidData.withDetails("Cities page have invalid format.")))
      } else {
        val citiesJs = citiesJsonAsOpt.get.value.toList

        if (citiesJs.exists(j => (j \ "Text").asOpt[String].isEmpty || (j \ "Value").asOpt[String].flatMap(s => Try(s.toInt).toOption).isEmpty)) {
          Log.error(s"""Failed to parse cities for country "$countryId". Found some invalid cities in page: $page""", "CityExtractor")
          Left(Errors(SingleError.InvalidData.withDetails("Invalid cities are found in page.")))
        } else {
          val cities = citiesJs map {
            cityJs =>
              val id       = (cityJs \ "Value").as[String].toInt
              val htmlName = Utils.sanitizeHtml((cityJs \ "Text").as[String])
              val name     = City.cityIdToTurkishNameMap.getOrElse(id, htmlName)

              City(id, countryId, name)
          }

          val sortedCities = cities.sortBy(_.name)

          Right(sortedCities)
        }
      }
    } catch {
      case t: Throwable =>
        Log.throwable(t, s"""Failed to parse cities for country "$countryId"!""", "CityExtractor")
        Left(Errors(SingleError.RequestFailed.withDetails(s"""Failed to parse cities for country "$countryId"!""")))
    }
  }
}
