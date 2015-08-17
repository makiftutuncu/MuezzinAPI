package com.mehmetakiftutuncu.muezzinapi.extractors.prayertimes

import com.mehmetakiftutuncu.muezzinapi.models.prayertimes.District
import com.mehmetakiftutuncu.muezzinapi.utilities.error.{Errors, SingleError}
import com.mehmetakiftutuncu.muezzinapi.utilities.{Conf, Log, Utils, Web}
import play.api.libs.json.{JsArray, JsValue}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

/**
 * An extractor to download and extract district names for a given city
 */
object DistrictExtractor {
  /**
   * Downloads and extracts districts for given city
   *
   * @param cityId Id of city whose districts to get
   *
   * @return Some errors or a list of districts
   */
  def extractDistricts(cityId: Int): Future[Either[Errors, List[District]]] = {
    Web.getForJson(Conf.Url.districts.format(cityId)) map {
      case Left(getPageErrors) =>
        Left(getPageErrors)

      case Right(districtsPage) =>
        parseDistricts(cityId, districtsPage)
    }
  }

  /**
   * Parses downloaded districts Json
   *
   * @param cityId Id of city these districts belong to
   * @param page   Districts as Json
   *
   * @return Some errors or a list of districts
   */
  private def parseDistricts(cityId: Int, page: JsValue): Either[Errors, List[District]] = {
    try {
      Log.debug(s"""Parsing districts for city "$cityId"...""", "DistrictExtractor")

      val districtsJsonAsOpt = page.asOpt[JsArray]

      if (districtsJsonAsOpt.isEmpty) {
        Log.error(s"""Failed to parse districts for city "$cityId". Page has invalid format: $page""", "DistrictExtractor")
        Left(Errors(SingleError.InvalidData.withDetails("Districts page have invalid format.")))
      } else {
        val districtsJs = districtsJsonAsOpt.get.value.toList

        if (districtsJs.exists(j => (j \ "Text").asOpt[String].isEmpty || (j \ "Value").asOpt[String].flatMap(s => Try(s.toInt).toOption).isEmpty)) {
          Log.error(s"""Failed to parse districts for city "$cityId". Found some invalid districts in page: $page""", "DistrictExtractor")
          Left(Errors(SingleError.InvalidData.withDetails("Invalid districts are found in page.")))
        } else {
          val districts = districtsJs map {
            districtJs =>
              val id   = (districtJs \ "Value").as[String].toInt
              val name = Utils.sanitizeHtml((districtJs \ "Text").as[String])

              District(id, cityId, name)
          }

          val sortedDistricts = districts.sortBy(_.name)

          Right(sortedDistricts)
        }
      }
    } catch {
      case t: Throwable =>
        Log.throwable(t, s"""Failed to parse districts for city "$cityId"!""", "DistrictExtractor")
        Left(Errors(SingleError.RequestFailed.withDetails(s"""Failed to parse districts for city "$cityId"!""")))
    }
  }
}
