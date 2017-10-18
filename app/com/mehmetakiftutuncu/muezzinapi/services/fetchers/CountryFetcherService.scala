package com.mehmetakiftutuncu.muezzinapi.services.fetchers

import java.time.Duration
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

@ImplementedBy(classOf[CountryFetcherService])
trait AbstractCountryFetcherService {
  def getCountries: Future[Maybe[List[Country]]]
}

@Singleton
class CountryFetcherService @Inject()(Conf: AbstractConf, WS: AbstractWS) extends AbstractCountryFetcherService with Logging {
  override def getCountries: Future[Maybe[List[Country]]] = {
    val log: String = "Failed to get countries!"

    try {
      Timer.start("fetchCountries")

      val url: String = Conf.getString("muezzinApi.diyanet.url", "")

      WS.url(url).get().map {
        wsResponse: WSResponse =>
          val fetchDuration: Duration = Timer.stop("fetchCountries")

          val status: Int         = wsResponse.status
          val contentType: String = wsResponse.header(HeaderNames.CONTENT_TYPE).getOrElse("")

          if (status != Status.OK) {
            val errors: Errors = Errors(CommonError.requestFailed.reason("Diyanet returned invalid status.").data(status.toString))
            Log.error(s"$log Status '$status', content type '$contentType', body: ${wsResponse.body}", errors)

            Maybe(errors)
          } else if (!contentType.contains(MimeTypes.HTML)) {
            val errors: Errors = Errors(CommonError.requestFailed.reason("Diyanet returned invalid content type.").data(contentType))
            Log.error(s"$log Status '$status', content type '$contentType', body: ${wsResponse.body}", errors)

            Maybe(errors)
          } else {
            val page: String = wsResponse.body

            val (parseDuration: Duration, maybeCountries: Maybe[List[Country]]) = Timer.time {
              parseCountries(page)
            }

            Log.debug(s"""Fetched countries in ${fetchDuration.toMillis} ms and parsed them in ${parseDuration.toMillis} ms.""")

            maybeCountries
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
      val countries: List[Country] = parseListOf[Country](page, "ulkeId") { case (id: Int, rawName: String) =>
        Country.idToNamesMap.get(id) match {
          case Some((name: String, nameTurkish: String, nameNative: String)) =>
            Country(id, name, nameTurkish, nameNative)

          case None =>
            Country(id, rawName, rawName, rawName)
        }
      }

      if (countries.isEmpty) {
        val errors: Errors = Errors(CommonError.notFound)
        Log.error(s"$log No countries were found. Page: $page", errors)

        Maybe(errors)
      } else {
        val sortedCountries: List[Country] = countries.sortBy(_.name)

        Maybe(sortedCountries)
      }
    } catch {
      case NonFatal(t: Throwable) =>
        val errors: Errors = Errors(CommonError("parseFailed").reason(t.getMessage))
        Log.error(s"$log Page: $page", errors)

        Maybe(errors)
    }
  }
}
