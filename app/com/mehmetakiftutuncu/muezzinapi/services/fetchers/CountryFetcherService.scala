package com.mehmetakiftutuncu.muezzinapi.services.fetchers

import javax.inject.{Inject, Singleton}

import com.github.mehmetakiftutuncu.errors.{CommonError, Errors, Maybe}
import com.google.inject.ImplementedBy
import com.mehmetakiftutuncu.muezzinapi.models.Country
import com.mehmetakiftutuncu.muezzinapi.utilities._
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.libs.ws.WSResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.matching.Regex

@ImplementedBy(classOf[CountryFetcherService])
trait AbstractCountryFetcherService {
  def getCountries: Future[Maybe[List[Country]]]
}

@Singleton
class CountryFetcherService @Inject()(Conf: AbstractConf, WS: AbstractWS) extends AbstractCountryFetcherService with Logging {
  override def getCountries: Future[Maybe[List[Country]]] = {
    val log: String = "Failed to get countries!"

    try {
      Log.debug("Fetching countries...")

      val url: String = Conf.getString("muezzinApi.url.countries", "")

      WS.url(url).get().map {
        wsResponse: WSResponse =>
          val status: Int         = wsResponse.status
          val contentType: String = wsResponse.header(HeaderNames.CONTENT_TYPE).getOrElse("")

          if (status != Status.OK) {
            val errors: Errors = Errors(CommonError.requestFailed.reason("Diyanet returned invalid status."))
            Log.error(s"$log Status '$status', content type '$contentType', body: ${wsResponse.body}", errors)

            Maybe(errors)
          } else if (!contentType.contains(MimeTypes.HTML)) {
            val errors: Errors = Errors(CommonError.requestFailed.reason("Diyanet returned content type."))
            Log.error(s"$log Status '$status', content type '$contentType', body: ${wsResponse.body}", errors)

            Maybe(errors)
          } else {
            val page: String = wsResponse.body

            parseCountries(page)
          }
      }.recover {
        case NonFatal(t: Throwable) =>
          val errors: Errors = Errors(CommonError.requestFailed.reason(t.getMessage))
          Log.error(s"$log Future failed!", errors)

          Maybe(errors)
      }
    } catch {
      case NonFatal(t: Throwable) =>
        val errors: Errors = Errors(CommonError.requestFailed.reason(t.getMessage))
        Log.error(log, errors)

        Future.successful(Maybe(errors))
    }
  }

  private def parseCountries(page: String): Maybe[List[Country]] = {
    val log: String = "Failed to parse all countries!"

    try {
      Log.debug("Parsing countries...")

      val countriesSelectRegex: Regex = """[\s\S]+<select.+?id="Country".+?>([\s\S]+?)<\/select>[\s\S]+""".r
      val countryOptionRegex: Regex   = """[\s\S]*?<option.+?value="(\d+)".*?>(.+?)<\/option>[\s\S]*?""".r

      val countriesSelectMatchAsOpt: Option[Regex.Match] = countriesSelectRegex.findFirstMatchIn(page)

      if (countriesSelectMatchAsOpt.isEmpty || countriesSelectMatchAsOpt.exists(_.groupCount < 1)) {
        val errors: Errors = Errors(CommonError("parseFailed").reason("Countries are not found."))
        Log.error(s"$log Page: $page", errors)

        Maybe(errors)
      } else {
        val countriesSelect: String                 = countriesSelectMatchAsOpt.get.group(1)
        val countryOptionMatches: List[Regex.Match] = countryOptionRegex.findAllMatchIn(countriesSelect).toList

        if (countryOptionMatches.isEmpty || countryOptionMatches.exists(_.groupCount < 2)) {
          val errors: Errors = Errors(CommonError("parseFailed").reason("Found some invalid countries."))
          Log.error(s"$log Page: $page", errors)

          Maybe(errors)
        } else {
          val countries: List[Country] = countryOptionMatches.map {
            countryOptionMatch: Regex.Match =>
              val id: Int = countryOptionMatch.group(1).toInt

              val rawName: String = HtmlSanitizer.sanitizeHtml(countryOptionMatch.group(2))

              Country.idToNamesMap.get(id) match {
                case Some((name: String, nameTurkish: String, nameNative: String)) =>
                  Country(id, name, nameTurkish, nameNative)

                case None =>
                  Country(id, rawName, rawName, rawName)
              }
          }

          val sortedCountries: List[Country] = countries.sortBy(_.name)

          Maybe(sortedCountries)
        }
      }
    } catch {
      case NonFatal(t: Throwable) =>
        val errors: Errors = Errors(CommonError("parseFailed").reason(t.getMessage))
        Log.error(s"$log Page: $page", errors)

        Maybe(errors)
    }
  }
}
