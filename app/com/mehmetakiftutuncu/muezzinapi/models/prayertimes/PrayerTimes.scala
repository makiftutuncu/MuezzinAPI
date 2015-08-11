package com.mehmetakiftutuncu.muezzinapi.models.prayertimes

import com.mehmetakiftutuncu.muezzinapi.models.base.{Jsonable, MuezzinAPIModel}
import com.mehmetakiftutuncu.muezzinapi.utilities.Log
import com.mehmetakiftutuncu.muezzinapi.utilities.error.{SingleError, Errors}
import org.joda.time.{DateTimeZone, DateTime}
import play.api.libs.json.{JsValue, Json}

case class PrayerTimes(date: DateTime,
                       fajr: DateTime,
                       shuruq: DateTime,
                       dhuhr: DateTime,
                       asr: DateTime,
                       maghrib: DateTime,
                       isha: DateTime,
                       qibla: DateTime) extends MuezzinAPIModel

object PrayerTimes extends Jsonable[PrayerTimes] {
  /**
   * Converts given object to Json
   *
   * @param prayerTimes Object that will be converted to Json
   *
   * @return Json representation of given object
   */
  override def toJson(prayerTimes: PrayerTimes): JsValue = {
    Json.obj(
      "date"     -> (prayerTimes.date.getMillis    / 1000),
      "fajr"     -> (prayerTimes.fajr.getMillis    / 1000),
      "shuruq"   -> (prayerTimes.shuruq.getMillis  / 1000),
      "dhuhr"    -> (prayerTimes.dhuhr.getMillis   / 1000),
      "asr"      -> (prayerTimes.asr.getMillis     / 1000),
      "maghrib"  -> (prayerTimes.maghrib.getMillis / 1000),
      "isha"     -> (prayerTimes.isha.getMillis    / 1000),
      "qibla"    -> (prayerTimes.qibla.getMillis   / 1000)
    )
  }

  /**
   * Tries to convert given Json to an object of current type
   *
   * @param json Json from which object will be generated
   *
   * @return Generated object or some errors
   */
  override def fromJson(json: JsValue): Either[Errors, PrayerTimes] = {
    try {
      val date     = new DateTime((json \ "date").as[Long]     * 1000, DateTimeZone.UTC)
      val fajr     = new DateTime((json \ "fajr").as[Long]     * 1000, DateTimeZone.UTC)
      val shuruq   = new DateTime((json \ "shuruq").as[Long]   * 1000, DateTimeZone.UTC)
      val dhuhr    = new DateTime((json \ "dhuhr").as[Long]    * 1000, DateTimeZone.UTC)
      val asr      = new DateTime((json \ "asr").as[Long]      * 1000, DateTimeZone.UTC)
      val maghrib  = new DateTime((json \ "maghrib").as[Long]  * 1000, DateTimeZone.UTC)
      val isha     = new DateTime((json \ "isha").as[Long]     * 1000, DateTimeZone.UTC)
      val qibla    = new DateTime((json \ "qibla").as[Long]    * 1000, DateTimeZone.UTC)

      Right(PrayerTimes(date, fajr, shuruq, dhuhr, asr, maghrib, isha, qibla))
    } catch {
      case t: Throwable =>
        Log.throwable(t, s"""Failed to convert "$json" to a prayer times!""", "PrayerTimes")
        Left(Errors(SingleError.InvalidData.withValue(json.toString()).withDetails("Invalid prayer times Json!")))
    }
  }
}
