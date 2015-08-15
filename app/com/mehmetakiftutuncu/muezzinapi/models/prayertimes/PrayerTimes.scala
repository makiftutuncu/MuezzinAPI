package com.mehmetakiftutuncu.muezzinapi.models.prayertimes

import com.mehmetakiftutuncu.muezzinapi.models.base.{Jsonable, MuezzinAPIModel}
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}

case class PrayerTimes(date: DateTime,
                       fajr: DateTime,
                       shuruq: DateTime,
                       dhuhr: DateTime,
                       asr: DateTime,
                       maghrib: DateTime,
                       isha: DateTime,
                       qibla: DateTime) extends MuezzinAPIModel with Jsonable[PrayerTimes] {
  /**
   * Converts this object to Json
   *
   * @return Json representation of this object
   */
  override def toJson: JsValue = Json.obj(
    "date"     -> (date.getMillis    / 1000),
    "fajr"     -> (fajr.getMillis    / 1000),
    "shuruq"   -> (shuruq.getMillis  / 1000),
    "dhuhr"    -> (dhuhr.getMillis   / 1000),
    "asr"      -> (asr.getMillis     / 1000),
    "maghrib"  -> (maghrib.getMillis / 1000),
    "isha"     -> (isha.getMillis    / 1000),
    "qibla"    -> (qibla.getMillis   / 1000)
  )
}

object PrayerTimes {

}
