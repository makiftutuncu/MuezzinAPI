package com.mehmetakiftutuncu.muezzinapi.extractors.prayertimes

import com.mehmetakiftutuncu.muezzinapi.models.prayertimes.PrayerTimes
import com.mehmetakiftutuncu.muezzinapi.utilities.error.{Errors, SingleError}
import com.mehmetakiftutuncu.muezzinapi.utilities.{Conf, Log, Web}
import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PrayerTimesExtractor {
  val dateRegex = """^(\d{2})\.(\d{2})\.(\d{4})$""".r
  val timeRegex = """^(\d{2}):(\d{2})$""".r

  def extractPrayerTimes(country: Int, city: Int, district: Int): Future[Either[Errors, List[PrayerTimes]]] = {
    Web.postForHtml(Conf.Url.prayerTimes,
                    Map(
                      "Country" -> Seq(country.toString),
                      "State" -> Seq(city.toString),
                      "City" -> Seq(district.toString),
                      "period" -> Seq("Aylik")
                    )) map {
      case Left(getPageErrors) =>
        Left(getPageErrors)

      case Right(prayerTimesPage) =>
        parsePrayerTimes(prayerTimesPage)
    }
  }

  private def parsePrayerTimes(page: String): Either[Errors, List[PrayerTimes]] = {
    val prayerTimesTableRegex = """[\s\S]+<table.+?>([\s\S]+?)<\/table>[\s\S]+""".r
    val prayerTimesRowRegex   = """[\s\S]*?<tr>\s*?<td.+?>(.+?)</td>\s*?<td.+?>(.+?)</td>\s*?<td.+?>(.+?)</td>\s*?<td.+?>(.+?)</td>\s*?<td.+?>(.+?)</td>\s*?<td.+?>(.+?)</td>\s*?<td.+?>(.+?)</td>\s*?<td.+?>(.+?)</td>\s*?</tr>[\s\S]*?""".r

    val prayerTimesTableMatchAsOpt = prayerTimesTableRegex.findFirstMatchIn(page)

    if (prayerTimesTableMatchAsOpt.isEmpty || prayerTimesTableMatchAsOpt.get.groupCount < 1) {
      Log.error("Failed to parse prayer times. Prayer times are not found in page: " + page, "PrayerTimesExtractor")
      Left(Errors(SingleError.RequestFailed.withDetails("Prayer times are not found in page.")))
    } else {
      val prayerTimesTable = prayerTimesTableMatchAsOpt.get.group(1)

      val prayerTimesRows = prayerTimesRowRegex.findAllMatchIn(prayerTimesTable).toList

      if (prayerTimesRows.isEmpty || prayerTimesRows.exists(m => m.groupCount < 8)) {
        Log.error("Failed to parse prayer times. Found some invalid prayer times in page: " + page, "PrayerTimesExtractor")
        Left(Errors(SingleError.InvalidData.withDetails("Invalid prayer times are not found in page.")))
      } else {
        val prayerTimes = prayerTimesRows map {
          prayerTimesRow =>
            val dateString    = prayerTimesRow.group(1)
            val fajrString    = prayerTimesRow.group(2)
            val shuruqString  = prayerTimesRow.group(3)
            val dhuhrString   = prayerTimesRow.group(4)
            val asrString     = prayerTimesRow.group(5)
            val maghribString = prayerTimesRow.group(6)
            val ishaString    = prayerTimesRow.group(7)
            val qiblaString   = prayerTimesRow.group(8)

            val date    = getDate(dateString)
            val fajr    = getTime(date, fajrString)
            val shuruq  = getTime(date, shuruqString)
            val dhuhr   = getTime(date, dhuhrString)
            val asr     = getTime(date, asrString)
            val maghrib = getTime(date, maghribString)
            val isha    = getTime(date, ishaString)
            val qibla   = getTime(date, qiblaString)

            PrayerTimes(date, fajr, shuruq, dhuhr, asr, maghrib, isha, qibla)
        }

        val sortedPrayerTimes = prayerTimes.sortBy(_.date.getMillis)

        Right(sortedPrayerTimes)
      }
    }
  }

  private def getDate(dateString: String): DateTime = {
    val dateParts          = dateRegex.findFirstMatchIn(dateString).get
    val (day, month, year) = (dateParts.group(1).toInt, dateParts.group(2).toInt, dateParts.group(3).toInt)

    new DateTime(year, month, day, 0, 0, 0, 0, DateTimeZone.UTC)
  }

  private def getTime(base: DateTime, timeString: String): DateTime = {
    val timeParts        = timeRegex.findFirstMatchIn(timeString).get
    val (hours, minutes) = (timeParts.group(1).toInt, timeParts.group(2).toInt)

    base.plusHours(hours).plusMinutes(minutes)
  }
}
