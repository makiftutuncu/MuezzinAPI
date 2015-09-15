package com.mehmetakiftutuncu.muezzinapi.models.prayertimes

import anorm.NamedParameter
import com.mehmetakiftutuncu.muezzinapi.models.base.Jsonable
import com.mehmetakiftutuncu.muezzinapi.utilities.error.{Errors, SingleError}
import com.mehmetakiftutuncu.muezzinapi.utilities.{Conf, Database, Log}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.{JsValue, Json}

/**
 * Represents prayer times for a day for a location
 *
 * @param countryId  Id of country this prayer times belongs to
 * @param cityId     Id of city this prayer times belongs to
 * @param districtId Id of district this prayer times belongs to
 * @param dayDate    A [[org.joda.time.DateTime]] representing the day with hours, minutes, seconds and milliseconds set to 0 and timezone to UTC
 * @param fajr       A [[org.joda.time.DateTime]] representing fajr time (dayDate + difference to fajr)
 * @param shuruq     A [[org.joda.time.DateTime]] representing shuruq time (dayDate + difference to shuruq)
 * @param dhuhr      A [[org.joda.time.DateTime]] representing dhuhr time (dayDate + difference to dhuhr)
 * @param asr        A [[org.joda.time.DateTime]] representing asr time (dayDate + difference to asr)
 * @param maghrib    A [[org.joda.time.DateTime]] representing maghrib time (dayDate + difference to maghrib)
 * @param isha       A [[org.joda.time.DateTime]] representing isha time (dayDate + difference to isha)
 * @param qibla      A [[org.joda.time.DateTime]] representing qibla time (dayDate + difference to qibla)
 */
case class PrayerTimes(countryId: Int,
                       cityId: Int,
                       districtId: Option[Int],
                       dayDate: DateTime,
                       fajr: DateTime,
                       shuruq: DateTime,
                       dhuhr: DateTime,
                       asr: DateTime,
                       maghrib: DateTime,
                       isha: DateTime,
                       qibla: DateTime) extends Jsonable[PrayerTimes] {
  /**
   * Converts this object to Json
   *
   * @return Json representation of this object
   */
  override def toJson: JsValue = Json.obj(
    "dayDate" -> dayDate.getMillis,
    "fajr"    -> fajr.getMillis,
    "shuruq"  -> shuruq.getMillis,
    "dhuhr"   -> dhuhr.getMillis,
    "asr"     -> asr.getMillis,
    "maghrib" -> maghrib.getMillis,
    "isha"    -> isha.getMillis,
    "qibla"   -> qibla.getMillis
  )
}

/**
 * Companion object of PrayerTimes
 */
object PrayerTimes {
  /**
   * Gets prayer times for a month for given country, city and district, from database
   *
   * @param countryId  Id of the country for which to get prayer times
   * @param cityId     Id of the city for which to get prayer times
   * @param districtId Id of the districty for which to get prayer times
   *
   * @return Some errors or a list of prayer times
   */
  def getAllFromDatabase(countryId: Int, cityId: Int, districtId: Option[Int]): Either[Errors, List[PrayerTimes]] = {
    Log.debug(s"""Getting prayer times for country "$countryId", city "$cityId" and district id "$districtId" from database...""", "PrayerTimes.getAllFromDatabase")

    try {
      val sql = anorm.SQL(
        """
          |SELECT *
          |FROM PrayerTimes
          |WHERE countryId = {countryId}
          |  AND cityId = {cityId}
          |  AND districtId = {districtId}
          |ORDER BY dayDate
        """.stripMargin).on("countryId" -> countryId, "cityId" -> cityId, "districtId" -> districtId)

      val prayerTimesList = Database.applyWithConnection(sql) map {
        row =>
          val dayDate: DateTime = new DateTime(row[Long]("dayDate"), DateTimeZone.UTC)
          val fajr: DateTime    = new DateTime(row[Long]("fajr"),    DateTimeZone.UTC)
          val shuruq: DateTime  = new DateTime(row[Long]("shuruq"),  DateTimeZone.UTC)
          val dhuhr: DateTime   = new DateTime(row[Long]("dhuhr"),   DateTimeZone.UTC)
          val asr: DateTime     = new DateTime(row[Long]("asr"),     DateTimeZone.UTC)
          val maghrib: DateTime = new DateTime(row[Long]("maghrib"), DateTimeZone.UTC)
          val isha: DateTime    = new DateTime(row[Long]("isha"),    DateTimeZone.UTC)
          val qibla: DateTime   = new DateTime(row[Long]("qibla"),   DateTimeZone.UTC)

          PrayerTimes(countryId, cityId, districtId, dayDate, fajr, shuruq, dhuhr, asr, maghrib, isha, qibla)
      }

      Right(prayerTimesList)
    } catch {
      case t: Throwable =>
        Log.throwable(t, s"""Failed to prayer times for country "$countryId", city "$cityId" and district id "$districtId" from database!""", "PrayerTimes.getAllFromDatabase")
        Left(Errors(SingleError.Database.withDetails(s"""Failed to prayer times for country "$countryId", city "$cityId" and district id "$districtId" from database!""")))
    }
  }

  /**
   * Saves given prayer times to database
   *
   * @param prayerTimesList Prayer times to save to database
   *
   * @return Non-empty errors if something goes wrong
   */
  def saveAllToDatabase(prayerTimesList: List[PrayerTimes]): Errors = {
    try {
      if (prayerTimesList.isEmpty) {
        Log.warn("Not saving empty list of prayer times...", "PrayerTimes.saveAllToDatabase")
        Errors.empty
      } else {
        Log.debug(s"""Saving all prayer times to database...""", "PrayerTimes.saveAllToDatabase")

        val valuesToParameters: List[(String, List[NamedParameter])] = prayerTimesList.zipWithIndex.foldLeft(List.empty[(String, List[NamedParameter])]) {
          case (valuesToParameters: List[(String, List[NamedParameter])], (prayerTimes: PrayerTimes, index: Int)) =>
            val countryIdKey: String  = s"countryId$index"
            val cityIdKey: String     = s"cityId$index"
            val districtIdKey: String = s"districtId$index"
            val dayDateKey: String    = s"dayDate$index"
            val fajrKey: String       = s"fajr$index"
            val shuruqKey: String     = s"shuruq$index"
            val dhuhrKey: String      = s"dhuhr$index"
            val asrKey: String        = s"asr$index"
            val maghribKey: String    = s"maghrib$index"
            val ishaKey: String       = s"isha$index"
            val qiblaKey: String      = s"qibla$index"

            valuesToParameters :+ {
              s"({countryId$index}, {cityId$index}, {districtId$index}, {dayDate$index}, {fajr$index}, {shuruq$index}, {dhuhr$index}, {asr$index}, {maghrib$index}, {isha$index}, {qibla$index})" -> List(
                NamedParameter(countryIdKey,  prayerTimes.countryId),
                NamedParameter(cityIdKey,     prayerTimes.cityId),
                NamedParameter(districtIdKey, prayerTimes.districtId),
                NamedParameter(dayDateKey,    prayerTimes.dayDate.getMillis),
                NamedParameter(fajrKey,       prayerTimes.fajr.getMillis),
                NamedParameter(shuruqKey,     prayerTimes.shuruq.getMillis),
                NamedParameter(dhuhrKey,      prayerTimes.dhuhr.getMillis),
                NamedParameter(asrKey,        prayerTimes.asr.getMillis),
                NamedParameter(maghribKey,    prayerTimes.maghrib.getMillis),
                NamedParameter(ishaKey,       prayerTimes.isha.getMillis),
                NamedParameter(qiblaKey,      prayerTimes.qibla.getMillis)
              )
            }
        }

        val valuesToParametersForDelete: List[(String, List[NamedParameter])] = prayerTimesList.zipWithIndex.foldLeft(List.empty[(String, List[NamedParameter])]) {
          case (valuesToParameters: List[(String, List[NamedParameter])], (prayerTimes: PrayerTimes, index: Int)) =>
            val countryIdKey: String  = s"countryId$index"
            val cityIdKey: String     = s"cityId$index"
            val districtIdKey: String = s"districtId$index"

            valuesToParameters :+ {
              s"(countryId = {countryId$index} AND cityId = {cityId$index} AND districtId = {districtId$index})" -> List(
                NamedParameter(countryIdKey,  prayerTimes.countryId),
                NamedParameter(cityIdKey,     prayerTimes.cityId),
                NamedParameter(districtIdKey, prayerTimes.districtId)
              )
            }
        }

        val deleteSql = anorm.SQL(
          s"""
             |DELETE FROM PrayerTimes ${valuesToParametersForDelete.map(_._1).mkString("WHERE (", " OR ", ")")}
           """.stripMargin
        ).on(valuesToParametersForDelete.flatMap(_._2):_*)

        val insertSql = anorm.SQL(
          s"""
             |INSERT INTO PrayerTimes (countryId, cityId, districtId, dayDate, fajr, shuruq, dhuhr, asr, maghrib, isha, qibla)
             |VALUES ${valuesToParameters.map(_._1).mkString(", ")}
           """.stripMargin
        ).on(valuesToParameters.flatMap(_._2):_*)

        val savedCount = Database.withTransaction {
          implicit connection =>
            Database.executeUpdate(deleteSql)
            Database.executeUpdate(insertSql)
        }

        if (savedCount != prayerTimesList.size) {
          Log.error(s"""Failed to save ${prayerTimesList.size} prayer times to database, affected row count was $savedCount!""", "PrayerTimes.saveAllToDatabase")
          Errors(SingleError.Database.withDetails("Failed to save some cities to database!"))
        } else {
          Errors.empty
        }
      }
    } catch {
      case t: Throwable =>
        Log.throwable(t, s"""Failed to save ${prayerTimesList.size} prayer times to database!""", "PrayerTimes.saveAllToDatabase")
        Errors(SingleError.Database.withDetails("Failed to save all prayer times to database!"))
    }
  }

  /**
   * Deletes prayer times older than given day
   *
   * @param howManyDays Number of days to define if prayer times is old
   *
   * @return Some errors or number of deleted prayer times
   */
  def wipeOldPrayerTimes(howManyDays: Int = Conf.Broom.strength.toDays.toInt): Either[Errors, Int] = {
    try {
      val timestampLimit = DateTime.now().withTime(0, 0, 0, 0).minusDays(howManyDays).getMillis

      val sql = anorm.SQL(
        """
          |DELETE FROM PrayerTimes
          |WHERE dayDate < {timestampLimit}
        """.stripMargin).on("timestampLimit" -> timestampLimit)

      val deletedRowCount = Database.executeUpdateWithConnection(sql)

      Right(deletedRowCount)
    } catch {
      case t: Throwable =>
        Log.throwable(t, s"""Failed to wipe $howManyDays old prayer times!""", "PrayerTimes.wipeOldPrayerTimes")
        Left(Errors(SingleError.Database.withDetails(s"""Failed to wipe $howManyDays old prayer times!""")))
    }
  }
}
