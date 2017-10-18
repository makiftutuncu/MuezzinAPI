package com.mehmetakiftutuncu.muezzinapi.services.fetchers

import java.time.Duration
import javax.inject.{Inject, Singleton}

import com.github.mehmetakiftutuncu.errors.{CommonError, Errors, Maybe}
import com.google.inject.ImplementedBy
import com.mehmetakiftutuncu.muezzinapi.models.{City, Place}
import com.mehmetakiftutuncu.muezzinapi.utilities._
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.libs.ws.WSResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

@ImplementedBy(classOf[CityFetcherService])
trait AbstractCityFetcherService {
  def getCities(countryId: Int): Future[Maybe[List[City]]]
}

@Singleton
class CityFetcherService @Inject()(Conf: AbstractConf, WS: AbstractWS) extends AbstractCityFetcherService with Logging {
  override def getCities(countryId: Int): Future[Maybe[List[City]]] = {
    val log: String = s"""Failed to get cities for country id "$countryId"!"""

    try {
      Timer.start(s"fetchCities.$countryId")

      val url: String = Conf.getString("muezzinApi.diyanet.url", "")

      WS.url(url).post(Place(countryId, None, None).toForm).map {
        wsResponse: WSResponse =>
          val fetchDuration: Duration = Timer.stop(s"fetchCities.$countryId")

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

            val (parseDuration: Duration, maybeCities: Maybe[List[City]]) = Timer.time {
              parseCities(countryId, page)
            }

            Log.debug(s"""Fetched cities for country id "$countryId" in ${fetchDuration.toMillis} ms and parsed them in ${parseDuration.toMillis} ms.""")

            maybeCities
          }
      }.recover {
        case NonFatal(t: Throwable) =>
          val errors: Errors = Errors(CommonError.requestFailed.reason(t.getMessage).data(countryId.toString))
          Log.error(s"$log Future failed!", errors)

          Maybe(errors)
      }
    } catch {
      case NonFatal(t: Throwable) =>
        val errors: Errors = Errors(CommonError.requestFailed.reason(t.getMessage).data(countryId.toString))
        Log.error(log, errors)

        Future.successful(Maybe(errors))
    }
  }

  private def parseCities(countryId: Int, page: String): Maybe[List[City]] = {
    val log: String = s"""Failed to parse cities for country "$countryId"!"""

    try {
      val cities: List[City] = parseListOf[City](page, "ilId") { case (id: Int, rawName: String) =>
        val name: String = City.idToNamesMap.getOrElse(id, HtmlSanitizer.sanitizeHtml(rawName))

        City(id, countryId, name)
      }

      if (cities.isEmpty) {
        val errors: Errors = Errors(CommonError.notFound.data(countryId.toString))
        Log.error(s"$log No cities were found. Page: $page", errors)

        Maybe(errors)
      } else {
        Maybe(cities)
      }
    } catch {
      case NonFatal(t: Throwable) =>
        val errors: Errors = Errors(CommonError("parseFailed").reason(t.getMessage).data(countryId.toString))
        Log.error(s"$log Page: $page", errors)

        Maybe(errors)
    }
  }
}
