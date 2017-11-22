package com.mehmetakiftutuncu.muezzinapi.services.fetchers

import java.time.Duration
import java.util.Locale
import javax.inject.{Inject, Singleton}

import com.github.mehmetakiftutuncu.errors.{CommonError, Errors, Maybe}
import com.google.inject.ImplementedBy
import com.mehmetakiftutuncu.muezzinapi.models.{City, District}
import com.mehmetakiftutuncu.muezzinapi.utilities._
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.libs.json.{JsArray, JsValue}
import play.api.libs.ws.WSResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

@ImplementedBy(classOf[DistrictFetcherService])
trait AbstractDistrictFetcherService {
  def getDistricts(countryId: Int, cityId: Int): Future[Maybe[List[District]]]
}

@Singleton
class DistrictFetcherService @Inject()(Conf: AbstractConf, WS: AbstractWS) extends AbstractDistrictFetcherService with Logging {
  override def getDistricts(countryId: Int, cityId: Int): Future[Maybe[List[District]]] = {
    val log: String = s"""Failed to get districts for country "$countryId" and city "$cityId"!"""

    try {
      Timer.start(s"fetchDistricts.$countryId.$cityId")

      val url: String = Conf.getString("muezzinApi.diyanet.districtsUrl", "")

      WS.url(url).withQueryString("ChangeType" -> "state", "CountryId" -> countryId.toString, "StateId" -> cityId.toString).get().map {
        wsResponse: WSResponse =>
          val fetchDuration: Duration = Timer.stop(s"fetchDistricts.$countryId.$cityId")

          val status: Int         = wsResponse.status
          val contentType: String = wsResponse.header(HeaderNames.CONTENT_TYPE).getOrElse("")

          if (status != Status.OK) {
            val errors: Errors = Errors(CommonError.requestFailed.reason("Diyanet returned invalid status.").data(status.toString))
            Log.error(s"$log Status '$status', content type '$contentType', body: ${wsResponse.body}", errors)

            Maybe(errors)
          } else if (!contentType.contains(MimeTypes.JSON)) {
            val errors: Errors = Errors(CommonError.requestFailed.reason("Diyanet returned invalid content type.").data(contentType))
            Log.error(s"$log Status '$status', content type '$contentType', body: ${wsResponse.body}", errors)

            Maybe(errors)
          } else {
            val page: JsValue = wsResponse.json

            val (parseDuration: Duration, maybeDistricts: Maybe[List[District]]) = Timer.time {
              parseDistricts(countryId, cityId, page)
            }

            Log.debug(s"""Fetched districts for country "$countryId" and city "$cityId" in ${fetchDuration.toMillis} ms and parsed them in ${parseDuration.toMillis} ms.""")

            maybeDistricts
          }
      }.recover {
        case NonFatal(t: Throwable) =>
          val errors: Errors = Errors(CommonError.requestFailed.reason(t.getMessage).data(s"$countryId.$cityId"))
          Log.error(s"$log Future failed!", errors)

          Maybe(errors)
      }
    } catch {
      case NonFatal(t: Throwable) =>
        val errors: Errors = Errors(CommonError.requestFailed.reason(t.getMessage).data(cityId.toString))
        Log.error(log, errors)

        Future.successful(Maybe(errors))
    }
  }

  private def parseDistricts(countryId: Int, cityId: Int, page: JsValue): Maybe[List[District]] = {
    val log: String = s"""Failed to parse districts for country "$countryId" and city "$cityId"!"""

    try {
      val districts: List[District] = (page \ "StateRegionList").as[JsArray].value.toList.map { cityJson: JsValue =>
        val locale: Locale  = if (City.idToNamesMap.contains(cityId)) new Locale("tr") else Locale.getDefault
        val id: Int         = (cityJson \ "IlceID").as[String].toInt
        val rawName: String = (cityJson \ "IlceAdi").as[String]
        val name: String    = HtmlSanitizer.sanitizeHtml(str = rawName, locale = locale)

        District(id, cityId, name)
      }

      if (districts.isEmpty) {
        val errors: Errors = Errors(CommonError.notFound.data(s"$countryId.$cityId"))
        Log.error(s"$log No districts were found. Page: $page", errors)

        Maybe(errors)
      } else {
        Maybe(districts)
      }
    } catch {
      case NonFatal(t: Throwable) =>
        val errors: Errors = Errors(CommonError("parseFailed").reason(t.getMessage).data(s"$countryId.$cityId"))
        Log.error(s"$log Page: $page", errors)

        Maybe(errors)
    }
  }
}
