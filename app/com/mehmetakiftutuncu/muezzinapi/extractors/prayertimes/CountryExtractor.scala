package com.mehmetakiftutuncu.muezzinapi.extractors.prayertimes

import com.mehmetakiftutuncu.muezzinapi.models.prayertimes.Country
import com.mehmetakiftutuncu.muezzinapi.utilities.error.{Errors, SingleError}
import com.mehmetakiftutuncu.muezzinapi.utilities.{Conf, Log, Utils, Web}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CountryExtractor {
  def extractCountries(): Future[Either[Errors, List[Country]]] = {
    Web.getForHtml(Conf.Url.countries) map {
      case Left(getPageErrors) =>
        Left(getPageErrors)

      case Right(worldPrayerTimesPage) =>
        parseCountries(worldPrayerTimesPage)
    }
  }

  private def parseCountries(page: String): Either[Errors, List[Country]] = {
    try {
      Log.debug(s"""Parsing countries...""", "CountryExtractor")

      val countriesSelectRegex = """[\s\S]+<select.+?id="Country".+?>([\s\S]+?)<\/select>[\s\S]+""".r
      val countryOptionRegex   = """[\s\S]*?<option.+?value="(\d+)".*?>(.+?)<\/option>[\s\S]*?""".r

      val countriesSelectMatchAsOpt = countriesSelectRegex.findFirstMatchIn(page)

      if (countriesSelectMatchAsOpt.isEmpty || countriesSelectMatchAsOpt.get.groupCount < 1) {
        Log.error("Failed to parse countries. Countries are not found in page: " + page, "CountryExtractor")
        Left(Errors(SingleError.RequestFailed.withDetails("Countries are not found in page.")))
      } else {
        val countriesSelect = countriesSelectMatchAsOpt.get.group(1)

        val countryOptions = countryOptionRegex.findAllMatchIn(countriesSelect).toList

        if (countryOptions.isEmpty || countryOptions.exists(m => m.groupCount < 2)) {
          Log.error("Failed to parse countries. Found some invalid countries in page: " + page, "CountryExtractor")
          Left(Errors(SingleError.InvalidData.withDetails("Invalid countries are not found in page.")))
        } else {
          val countryList = countryOptions map {
            countryOption =>
              val id       = countryOption.group(1).toInt
              val htmlName = Utils.sanitizeHtml(countryOption.group(2))
              val name     = Country.countryIdToNameMap.getOrElse(id, htmlName)
              val trName   = Country.countryIdToTurkishNameMap.getOrElse(id, htmlName)

              Country(id, name, trName)
          }

          val sortedCountryList = countryList.sortBy(_.name)

          Right(sortedCountryList)
        }
      }
    } catch {
      case t: Throwable =>
        Log.throwable(t, s"""Failed to parse countries!""", "CountryExtractor")
        Left(Errors(SingleError.RequestFailed.withDetails(s"""Failed to parse countries!""")))
    }
  }
}
