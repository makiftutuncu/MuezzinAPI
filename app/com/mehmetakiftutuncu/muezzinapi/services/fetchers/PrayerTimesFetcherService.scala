package com.mehmetakiftutuncu.muezzinapi.services.fetchers

import java.time.{Duration, LocalDate}

import com.github.mehmetakiftutuncu.errors.{CommonError, Errors, Maybe}
import com.google.inject.ImplementedBy
import com.mehmetakiftutuncu.muezzinapi.models.{Place, PrayerTimesOfDay}
import com.mehmetakiftutuncu.muezzinapi.services.filterOutOldPrayerTimes
import com.mehmetakiftutuncu.muezzinapi.utilities._
import javax.inject.{Inject, Singleton}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.libs.ws.WSResponse

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

@ImplementedBy(classOf[PrayerTimesFetcherService])
trait AbstractPrayerTimesFetcherService {
  def getPrayerTimes(place: Place): Future[Maybe[List[PrayerTimesOfDay]]]
}

@Singleton
class PrayerTimesFetcherService @Inject()(Conf: AbstractConf, WS: AbstractWS) extends AbstractPrayerTimesFetcherService with Logging {
  override def getPrayerTimes(place: Place): Future[Maybe[List[PrayerTimesOfDay]]] = {
    val log: String = s"Failed to get prayer times for ${place.toLog}!"

    try {
      Timer.start(s"fetchPrayerTimes.${place.toKey}")

      val url: String = Conf.getString("muezzinApi.diyanet.prayerTimesUrl", "").format(place.districtId.map(_.toString).getOrElse(""))

      WS.url(url).get().map {
        wsResponse: WSResponse =>
          val fetchDuration: Duration = Timer.stop(s"fetchPrayerTimes.${place.toKey}")

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

            val (parseDuration: Duration, maybePrayerTimes: Maybe[List[PrayerTimesOfDay]]) = Timer.time {
              parsePrayerTimes(place, page)
            }

            Log.debug(s"Fetched prayer times for ${place.toLog} in ${fetchDuration.toMillis} ms and parsed them in ${parseDuration.toMillis} ms.")

            maybePrayerTimes
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

  private def parsePrayerTimes(place: Place, page: String): Maybe[List[PrayerTimesOfDay]] = {
    val log: String = s"Failed to parse prayer times for ${place.toLog}!"

    try {
      val document: Document = Jsoup.parse(page)
      val listOfTrArrays: List[Array[Element]] = document.select(".vakit-table").select("tr").asScala.toList.map(_.select("td").asScala.toArray)

      val prayerTimes: List[PrayerTimesOfDay] = listOfTrArrays.filter(_.nonEmpty).map { trArrayForDay: Array[Element] =>
        val date: String    = trArrayForDay(0).text()
        val fajr: String    = trArrayForDay(1).text()
        val shuruq: String  = trArrayForDay(2).text()
        val dhuhr: String   = trArrayForDay(3).text()
        val asr: String     = trArrayForDay(4).text()
        val maghrib: String = trArrayForDay(5).text()
        val isha: String    = trArrayForDay(6).text()

        PrayerTimesOfDay(LocalDate.parse(date, PrayerTimesOfDay.diyanetDateFormatter), fajr, shuruq, dhuhr, asr, maghrib, isha, None)
      }

      Maybe(filterOutOldPrayerTimes(prayerTimes))
    } catch {
      case NonFatal(t: Throwable) =>
        val errors: Errors = Errors(CommonError("parseFailed").reason(t.getMessage))
        Log.error(s"$log Page: $page", errors)

        Maybe(errors)
    }
  }
}
