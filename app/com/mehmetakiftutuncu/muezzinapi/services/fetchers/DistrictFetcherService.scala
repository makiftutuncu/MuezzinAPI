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
  def getDistricts(cityId: Int): Future[Maybe[List[District]]]
}

@Singleton
class DistrictFetcherService @Inject()(Conf: AbstractConf, WS: AbstractWS) extends AbstractDistrictFetcherService with Logging {
  override def getDistricts(cityId: Int): Future[Maybe[List[District]]] = {
    val log: String = s"""Failed to get districts for city id "$cityId"!"""

    try {
      Timer.start(s"fetchDistricts.$cityId")

      val url: String = Conf.getString("muezzinApi.url.districts", "").format(cityId)

      WS.url(url).get().map {
        wsResponse: WSResponse =>
          val fetchDuration: Duration = Timer.stop(s"fetchDistricts.$cityId")

          val status: Int         = wsResponse.status
          val contentType: String = wsResponse.header(HeaderNames.CONTENT_TYPE).getOrElse("")

          if (status != Status.OK) {
            val errors: Errors = Errors(CommonError.requestFailed.reason("Diyanet returned invalid status.").data(cityId.toString))
            Log.error(s"$log Status '$status', content type '$contentType', body: ${wsResponse.body}", errors)

            Maybe(errors)
          } else if (!contentType.contains(MimeTypes.JSON)) {
            val errors: Errors = Errors(CommonError.requestFailed.reason("Diyanet returned content type.").data(cityId.toString))
            Log.error(s"$log Status '$status', content type '$contentType', body: ${wsResponse.body}", errors)

            Maybe(errors)
          } else {
            val json: JsValue = wsResponse.json

            val (parseDuration: Duration, maybeDistricts: Maybe[List[District]]) = Timer.time {
              parseDistricts(cityId, json)
            }

            Log.debug(s"""Fetched districts for city id "$cityId" in ${fetchDuration.toMillis} ms and parsed them in ${parseDuration.toMillis} ms.""")

            maybeDistricts
          }
      }.recover {
        case NonFatal(t: Throwable) =>
          val errors: Errors = Errors(CommonError.requestFailed.reason(t.getMessage).data(cityId.toString))
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

  private def parseDistricts(cityId: Int, json: JsValue): Maybe[List[District]] = {
    val log: String = s"""Failed to parse districts for city id "$cityId"!"""

    try {
      val districtJsonsAsOpt: Option[JsArray] = json.asOpt[JsArray]

      if (districtJsonsAsOpt.isEmpty) {
        val errors: Errors = Errors(CommonError("parseFailed").reason("Districts are not found.").data(cityId.toString))
        Log.error(s"$log Json: $json", errors)

        Maybe(errors)
      } else {
        val districtJsons: List[JsValue] = districtJsonsAsOpt.get.value.toList

        val maybeDistricts: Maybe[List[District]] = districtJsons.foldLeft(Maybe(List.empty[District])) {
          case (maybeCurrentDistricts: Maybe[List[District]], districtJson: JsValue) =>
            val idStringAsOpt: Option[String] = (districtJson \ "Value").asOpt[String]
            val nameAsOpt: Option[String]     = (districtJson \ "Text").asOpt[String]

            if (idStringAsOpt.isEmpty || nameAsOpt.isEmpty) {
              val error: CommonError = CommonError.invalidData.data(districtJson.toString())

              Maybe(maybeCurrentDistricts.maybeErrors.map(_ + error).getOrElse(Errors.empty))
            } else if (maybeCurrentDistricts.hasErrors) {
              maybeCurrentDistricts
            } else {
              val id: Int      = idStringAsOpt.get.toInt
              val name: String = HtmlSanitizer.sanitizeHtml(
                str = nameAsOpt.get,
                locale = {if (City.idToNamesMap.contains(cityId)) new Locale("tr") else Locale.getDefault}
              )

              val district: District = District(id, cityId, name)

              Maybe(maybeCurrentDistricts.value :+ district)
            }
        }

        if (maybeDistricts.hasErrors) {
          Log.error(s"$log Json: $json", maybeDistricts.errors)
        }

        maybeDistricts
      }
    } catch {
      case NonFatal(t: Throwable) =>
        val errors: Errors = Errors(CommonError("parseFailed").reason(t.getMessage).data(cityId.toString))
        Log.error(s"$log Json: $json", errors)

        Maybe(errors)
    }
  }
}
