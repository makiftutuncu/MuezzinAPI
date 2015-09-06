package com.mehmetakiftutuncu.muezzinapi.extractors.prayertimes

import com.mehmetakiftutuncu.muezzinapi.models.prayertimes.Country
import com.mehmetakiftutuncu.muezzinapi.utilities.error.{Errors, SingleError}
import com.mehmetakiftutuncu.muezzinapi.utilities.{Conf, Log, Utils, Web}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * An extractor to download and extract country names
 */
object CountryExtractor {
  /**
   * Downloads and extracts countries
   *
   * @return Some errors or a list of countries
   */
  def extractCountries(): Future[Either[Errors, List[Country]]] = {
    Web.getForHtml(Conf.Url.countries) map {
      case Left(getPageErrors) =>
        Left(getPageErrors)

      case Right(worldPrayerTimesPage) =>
        parseCountries(worldPrayerTimesPage)
    }
  }

  /**
   * Parses downloaded countries HTML
   *
   * @param page Countries as HTML
   *
   * @return Some errors or a list of countries
   */
  private def parseCountries(page: String): Either[Errors, List[Country]] = {
    try {
      Log.debug(s"""Parsing countries...""", "CountryExtractor.parseCountries")

      val countriesSelectRegex = """[\s\S]+<select.+?id="Country".+?>([\s\S]+?)<\/select>[\s\S]+""".r
      val countryOptionRegex   = """[\s\S]*?<option.+?value="(\d+)".*?>(.+?)<\/option>[\s\S]*?""".r

      val countriesSelectMatchAsOpt = countriesSelectRegex.findFirstMatchIn(page)

      if (countriesSelectMatchAsOpt.isEmpty || countriesSelectMatchAsOpt.get.groupCount < 1) {
        Log.error("Failed to parse countries. Countries are not found in page: " + page, "CountryExtractor.parseCountries")
        Left(Errors(SingleError.RequestFailed.withDetails("Countries are not found in page.")))
      } else {
        val countriesSelect = countriesSelectMatchAsOpt.get.group(1)

        val countryOptions = countryOptionRegex.findAllMatchIn(countriesSelect).toList

        if (countryOptions.isEmpty || countryOptions.exists(m => m.groupCount < 2)) {
          Log.error("Failed to parse countries. Found some invalid countries in page: " + page, "CountryExtractor.parseCountries")
          Left(Errors(SingleError.InvalidData.withDetails("Invalid countries are not found in page.")))
        } else {
          val countryList = countryOptions map {
            countryOption =>
              val id         = countryOption.group(1).toInt
              val htmlName   = Utils.sanitizeHtml(countryOption.group(2))
              val name       = Country.countryIdToNameMap.getOrElse(id, htmlName)
              val trName     = Country.countryIdToTurkishNameMap.getOrElse(id, htmlName)
              val nativeName = Country.countryIdToNativeNameMap.getOrElse(id, htmlName)

              Country(id, name, trName, nativeName)
          }

          val sortedCountryList = countryList.sortBy(_.name)

          Right(sortedCountryList)
        }
      }
    } catch {
      case t: Throwable =>
        Log.throwable(t, s"""Failed to parse countries!""", "CountryExtractor.parseCountries")
        Left(Errors(SingleError.RequestFailed.withDetails(s"""Failed to parse countries!""")))
    }
  }
}
