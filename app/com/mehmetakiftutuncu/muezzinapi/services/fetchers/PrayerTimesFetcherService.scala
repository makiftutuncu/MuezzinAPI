package com.mehmetakiftutuncu.muezzinapi.services.fetchers

import java.time.{Duration, LocalDate}
import javax.inject.{Inject, Singleton}

import com.github.mehmetakiftutuncu.errors.{CommonError, Errors, Maybe}
import com.google.inject.ImplementedBy
import com.mehmetakiftutuncu.muezzinapi.models.{Place, PrayerTimesOfDay}
import com.mehmetakiftutuncu.muezzinapi.utilities._
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.libs.ws.WSResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.matching.Regex
import scala.util.matching.Regex.Match

@ImplementedBy(classOf[PrayerTimesFetcherService])
trait AbstractPrayerTimesFetcherService {
  def getPrayerTimes(place: Place): Future[Maybe[List[PrayerTimesOfDay]]]
}

@Singleton
class PrayerTimesFetcherService @Inject()(Conf: AbstractConf, WS: AbstractWS) extends AbstractPrayerTimesFetcherService with Logging {
  private val prayerTimesTableRegex: Regex = """[\s\S]+<table.+?>([\s\S]+?)<\/table>[\s\S]+""".r
  private val prayerTimesRowRegex: Regex   = """[\s\S]*?<tr>\s*?<td.+?>(.+?)</td>\s*?<td.+?>(.+?)</td>\s*?<td.+?>(.+?)</td>\s*?<td.+?>(.+?)</td>\s*?<td.+?>(.+?)</td>\s*?<td.+?>(.+?)</td>\s*?<td.+?>(.+?)</td>\s*?<td.+?>(.+?)</td>\s*?</tr>[\s\S]*?""".r

  override def getPrayerTimes(place: Place): Future[Maybe[List[PrayerTimesOfDay]]] = {
    val log: String = s"Failed to get prayer times for ${place.toLog}!"

    try {
      Timer.start(s"fetchPrayerTimes.${place.toPath}")

      val url: String = Conf.getString("muezzinApi.url.prayerTimes", "")

      WS.url(url).post(place.toForm).map {
        wsResponse: WSResponse =>
          val fetchDuration: Duration = Timer.stop(s"fetchPrayerTimes.${place.toPath}")

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
      val prayerTimesTableMatchAsOpt: Option[Match] = prayerTimesTableRegex.findFirstMatchIn(page)

      if (prayerTimesTableMatchAsOpt.isEmpty) {
        val errors: Errors = Errors(CommonError("parseFailed").reason("Prayer times table is not found in page!"))
        Log.error(s"$log Page: $page", errors)
        Maybe(errors)
      } else {
        val prayerTimesTableString: String = prayerTimesTableMatchAsOpt.get.group(1)

        val prayerTimesRowMatches: List[Match] = prayerTimesRowRegex.findAllMatchIn(prayerTimesTableString).toList

        if (prayerTimesRowMatches.isEmpty || prayerTimesRowMatches.exists(_.groupCount < 8)) {
          val errors: Errors = Errors(CommonError("parseFailed").reason("Found some invalid prayer times in page!"))
          Log.error(s"$log Page: $page", errors)
          Maybe(errors)
        } else {
          val prayerTimes: List[PrayerTimesOfDay] = prayerTimesRowMatches.map {
            prayerTimesRowMatch: Match =>
              val date: String    = prayerTimesRowMatch.group(1)
              val fajr: String    = prayerTimesRowMatch.group(2)
              val shuruq: String  = prayerTimesRowMatch.group(3)
              val dhuhr: String   = prayerTimesRowMatch.group(4)
              val asr: String     = prayerTimesRowMatch.group(5)
              val maghrib: String = prayerTimesRowMatch.group(6)
              val isha: String    = prayerTimesRowMatch.group(7)
              val qibla: String   = prayerTimesRowMatch.group(8)

              PrayerTimesOfDay(LocalDate.parse(date, PrayerTimesOfDay.diyanetDateFormatter), fajr, shuruq, dhuhr, asr, maghrib, isha, qibla)
          }

          Maybe(prayerTimes)
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
