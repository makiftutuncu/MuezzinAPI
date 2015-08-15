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

  def extractPrayerTimes(countryId: Int, cityId: Int, districtId: Option[Int]): Future[Either[Errors, List[PrayerTimes]]] = {
    Web.postForHtml(Conf.Url.prayerTimes,
                    Map(
                      "Country" -> Seq(countryId.toString),
                      "State" -> Seq(cityId.toString),
                      "City" -> Seq(districtId.getOrElse(0).toString),
                      "period" -> Seq("Aylik")
                    )) map {
      case Left(getPageErrors) =>
        Left(getPageErrors)

      case Right(prayerTimesPage) =>
        parsePrayerTimes(countryId, cityId, districtId, prayerTimesPage)
    }
  }

  private def parsePrayerTimes(countryId: Int, cityId: Int, districtId: Option[Int], page: String): Either[Errors, List[PrayerTimes]] = {
    val prayerTimesTableRegex = """[\s\S]+<table.+?>([\s\S]+?)<\/table>[\s\S]+""".r
    val prayerTimesRowRegex   = """[\s\S]*?<tr>\s*?<td.+?>(.+?)</td>\s*?<td.+?>(.+?)</td>\s*?<td.+?>(.+?)</td>\s*?<td.+?>(.+?)</td>\s*?<td.+?>(.+?)</td>\s*?<td.+?>(.+?)</td>\s*?<td.+?>(.+?)</td>\s*?<td.+?>(.+?)</td>\s*?</tr>[\s\S]*?""".r

    val prayerTimesTableMatchAsOpt = prayerTimesTableRegex.findFirstMatchIn(page)

    if (prayerTimesTableMatchAsOpt.isEmpty || prayerTimesTableMatchAsOpt.get.groupCount < 1) {
      Log.error(s"""Failed to parse prayer times for country "$countryId", city "$cityId" and district id "$districtId". Prayer times are not found in $page""", "PrayerTimesExtractor")
      Left(Errors(SingleError.RequestFailed.withDetails("Prayer times are not found in page.")))
    } else {
      val prayerTimesTable = prayerTimesTableMatchAsOpt.get.group(1)

      val prayerTimesRows = prayerTimesRowRegex.findAllMatchIn(prayerTimesTable).toList

      if (prayerTimesRows.isEmpty || prayerTimesRows.exists(m => m.groupCount < 8)) {
        Log.error(s"""Failed to parse prayer times for country "$countryId", city "$cityId" and district id "$districtId". Found some invalid prayer times in $page""", "PrayerTimesExtractor")
        Left(Errors(SingleError.InvalidData.withDetails("Invalid prayer times are not found in page.")))
      } else {
        val prayerTimes = prayerTimesRows map {
          prayerTimesRow =>
            val dayDateString = prayerTimesRow.group(1)
            val fajrString    = prayerTimesRow.group(2)
            val shuruqString  = prayerTimesRow.group(3)
            val dhuhrString   = prayerTimesRow.group(4)
            val asrString     = prayerTimesRow.group(5)
            val maghribString = prayerTimesRow.group(6)
            val ishaString    = prayerTimesRow.group(7)
            val qiblaString   = prayerTimesRow.group(8)

            val dayDate = getDate(dayDateString)
            val fajr    = getTime(dayDate, fajrString)
            val shuruq  = getTime(dayDate, shuruqString)
            val dhuhr   = getTime(dayDate, dhuhrString)
            val asr     = getTime(dayDate, asrString)
            val maghrib = getTime(dayDate, maghribString)
            val isha    = getTime(dayDate, ishaString)
            val qibla   = getTime(dayDate, qiblaString)

            PrayerTimes(countryId, cityId, districtId, dayDate, fajr, shuruq, dhuhr, asr, maghrib, isha, qibla)
        }

        val sortedPrayerTimes = prayerTimes.sortBy(_.dayDate.getMillis)

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
