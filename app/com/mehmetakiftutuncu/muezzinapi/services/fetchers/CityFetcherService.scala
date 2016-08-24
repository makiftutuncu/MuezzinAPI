package com.mehmetakiftutuncu.muezzinapi.services.fetchers

import java.time.Duration
import javax.inject.{Inject, Singleton}

import com.github.mehmetakiftutuncu.errors.{CommonError, Errors, Maybe}
import com.google.inject.ImplementedBy
import com.mehmetakiftutuncu.muezzinapi.models.City
import com.mehmetakiftutuncu.muezzinapi.utilities._
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.libs.json.{JsArray, JsValue}
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

      val url: String = Conf.getString("muezzinApi.url.cities", "").format(countryId)

      WS.url(url).get().map {
        wsResponse: WSResponse =>
          val fetchDuration: Duration = Timer.stop(s"fetchCities.$countryId")

          val status: Int         = wsResponse.status
          val contentType: String = wsResponse.header(HeaderNames.CONTENT_TYPE).getOrElse("")

          if (status != Status.OK) {
            val errors: Errors = Errors(CommonError.requestFailed.reason("Diyanet returned invalid status.").data(countryId.toString))
            Log.error(s"$log Status '$status', content type '$contentType', body: ${wsResponse.body}", errors)

            Maybe(errors)
          } else if (!contentType.contains(MimeTypes.JSON)) {
            val errors: Errors = Errors(CommonError.requestFailed.reason("Diyanet returned content type.").data(countryId.toString))
            Log.error(s"$log Status '$status', content type '$contentType', body: ${wsResponse.body}", errors)

            Maybe(errors)
          } else {
            val json: JsValue = wsResponse.json

            val (parseDuration: Duration, maybeCities: Maybe[List[City]]) = Timer.time {
              parseCities(countryId, json)
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

  private def parseCities(countryId: Int, json: JsValue): Maybe[List[City]] = {
    val log: String = s"""Failed to parse cities for country id "$countryId"!"""

    try {
      val cityJsonsAsOpt: Option[JsArray] = json.asOpt[JsArray]

      if (cityJsonsAsOpt.isEmpty) {
        val errors: Errors = Errors(CommonError("parseFailed").reason("Cities are not found.").data(countryId.toString))
        Log.error(s"$log Json: $json", errors)

        Maybe(errors)
      } else {
        val cityJsons: List[JsValue] = cityJsonsAsOpt.get.value.toList

        val maybeCities: Maybe[List[City]] = cityJsons.foldLeft(Maybe(List.empty[City])) {
          case (maybeCurrentCities: Maybe[List[City]], cityJson: JsValue) =>
            val idStringAsOpt: Option[String] = (cityJson \ "Value").asOpt[String]
            val nameAsOpt: Option[String]     = (cityJson \ "Text").asOpt[String]

            if (idStringAsOpt.isEmpty || nameAsOpt.isEmpty) {
              val error: CommonError = CommonError.invalidData.data(cityJson.toString())

              Maybe(maybeCurrentCities.maybeErrors.map(_ + error).getOrElse(Errors.empty))
            } else if (maybeCurrentCities.hasErrors) {
              maybeCurrentCities
            } else {
              val id: Int      = idStringAsOpt.get.toInt
              val name: String = City.idToNamesMap.getOrElse(id, HtmlSanitizer.sanitizeHtml(nameAsOpt.get))

              val city: City = City(id, countryId, name)

              Maybe(maybeCurrentCities.value :+ city)
            }
        }

        if (maybeCities.hasErrors) {
          Log.error(s"$log Json: $json", maybeCities.errors)
        }

        maybeCities
      }
    } catch {
      case NonFatal(t: Throwable) =>
        val errors: Errors = Errors(CommonError("parseFailed").reason(t.getMessage).data(countryId.toString))
        Log.error(s"$log Json: $json", errors)

        Maybe(errors)
    }
  }
}
