package com.mehmetakiftutuncu.muezzinapi.extractors.prayertimes

import com.mehmetakiftutuncu.muezzinapi.models.prayertimes.City
import com.mehmetakiftutuncu.muezzinapi.utilities.error.{Errors, SingleError}
import com.mehmetakiftutuncu.muezzinapi.utilities.{Conf, Log, Utils, Web}
import play.api.libs.json.{JsArray, JsValue}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

object CityExtractor {
  def extractCities(country: Int): Future[Either[Errors, List[City]]] = {
    Web.getForJson(Conf.Url.citiesWithCountry.format(country)) map {
      case Left(getPageErrors) =>
        Left(getPageErrors)

      case Right(citiesPage) =>
        parseCities(citiesPage)
    }
  }

  private def parseCities(page: JsValue): Either[Errors, List[City]] = {
    val citiesJsonAsOpt = page.asOpt[JsArray]

    if (citiesJsonAsOpt.isEmpty) {
      Log.error(s"""Failed to parse cities. Page has invalid format: $page""", "CityExtractor")
      Left(Errors(SingleError.InvalidData.withDetails("Cities page have invalid format.")))
    } else {
      val citiesJs = citiesJsonAsOpt.get.value.toList

      if (citiesJs.exists(j => (j \ "Text").asOpt[String].isEmpty || (j \ "Value").asOpt[String].flatMap(s => Try(s.toInt).toOption).isEmpty)) {
        Log.error(s"""Failed to parse cities. Found some invalid cities in page: $page""", "CityExtractor")
        Left(Errors(SingleError.InvalidData.withDetails("Invalid cities are found in page.")))
      } else {
        val cities = citiesJs map {
          cityJs =>
            val id   = (cityJs \ "Value").as[String].toInt
            val name = Utils.sanitizeHtml((cityJs \ "Text").as[String])

            City(id, name)
        }

        val sortedCities = cities.sortBy(_.name)

        Right(sortedCities)
      }
    }
  }
}
