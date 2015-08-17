package com.mehmetakiftutuncu.muezzinapi.extractors.prayertimes

import com.mehmetakiftutuncu.muezzinapi.models.prayertimes.PrayerTimes
import com.mehmetakiftutuncu.muezzinapi.utilities.error.{Errors, SingleError}
import com.mehmetakiftutuncu.muezzinapi.utilities.{Conf, Log, Web}
import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * An extractor to download and extract prayer times for a given country, city and district
 */
object PrayerTimesExtractor {
  /** Date formatted as dd.MM.yyyy */
  val dateRegex = """^(\d{2})\.(\d{2})\.(\d{4})$""".r
  /** Time formatted HH:mm */
  val timeRegex = """^(\d{2}):(\d{2})$""".r

  /**
   * Downloads and extracts prayer times for given country, city and district
   *
   * @param countryId  Id of country whose prayer times to get
   * @param cityId     Id of city whose prayer times to get
   * @param districtId Id of district whose prayer times to get
   *
   * @return Some errors or a list of prayer times
   */
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

  /**
   * Parses downloaded prayer times HTML
   *
   * @param countryId  Id of country these prayer times belong to
   * @param cityId     Id of city these prayer times belong to
   * @param districtId Id of district these prayer times belong to
   * @param page       Prayer times as HTML
   *
   * @return Some errors or a list of prayer times
   */
  private def parsePrayerTimes(countryId: Int, cityId: Int, districtId: Option[Int], page: String): Either[Errors, List[PrayerTimes]] = {
    try {
      Log.debug(s"""Parsing prayer times for country "$countryId", city "$cityId" and district "$districtId"...""", "PrayerTimesExtractor")

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
    } catch {
      case t: Throwable =>
        Log.throwable(t, s"""Failed to parse prayer times for country "$countryId", city "$cityId" and district "$districtId"!""", "PrayerTimesExtractor")
        Left(Errors(SingleError.RequestFailed.withDetails(s"""Failed to parse prayer times for country "$countryId", city "$cityId" and district "$districtId"!""")))
    }
  }

  /**
   * Parses given date string
   *
   * @param dateString Date as a formatted string
   *
   * @return Parsed [[org.joda.time.DateTime]] with hours, minutes, seconds and milliseconds set to 0 and timezone to UTC
   */
  private def getDate(dateString: String): DateTime = {
    val dateParts          = dateRegex.findFirstMatchIn(dateString).get
    val (day, month, year) = (dateParts.group(1).toInt, dateParts.group(2).toInt, dateParts.group(3).toInt)

    new DateTime(year, month, day, 0, 0, 0, 0, DateTimeZone.UTC)
  }

  /**
   * Parses given time and gets shifted time according to a base time
   *
   * @param base       Base [[org.joda.time.DateTime]]
   * @param timeString Time as a formatted string
   *
   * @return Parsed and shifted [[org.joda.time.DateTime]] with hours and minutes of parsed value added to base
   */
  private def getTime(base: DateTime, timeString: String): DateTime = {
    val timeParts        = timeRegex.findFirstMatchIn(timeString).get
    val (hours, minutes) = (timeParts.group(1).toInt, timeParts.group(2).toInt)

    base.plusHours(hours).plusMinutes(minutes)
  }
}
