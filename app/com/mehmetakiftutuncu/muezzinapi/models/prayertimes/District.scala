package com.mehmetakiftutuncu.muezzinapi.models.prayertimes

import anorm.NamedParameter
import com.mehmetakiftutuncu.muezzinapi.models.base.Jsonable
import com.mehmetakiftutuncu.muezzinapi.utilities.error.{Errors, SingleError}
import com.mehmetakiftutuncu.muezzinapi.utilities.{Database, Log}
import play.api.libs.json.{JsValue, Json}

/**
 * Represents a district in a city
 *
 * @param id     Id of the district as a number
 * @param cityId Id of the city this district belongs to
 * @param name   Name of the district
 */
case class District(id: Int, cityId: Int, name: String) extends Jsonable[District] {
  /**
   * Converts this object to Json
   *
   * @return Json representation of this object
   */
  override def toJson: JsValue = Json.obj("id" -> id, "name" -> name)
}

/**
 * Companion object of District
 */
object District extends {
  /**
   * Gets all districts from database
   *
   * @param cityId Id of the city for which to get districts
   *
   * @return Some errors or a list of districts
   */
  def getAllFromDatabase(cityId: Int): Either[Errors, List[District]] = {
    Log.debug(s"""Getting all districts for city "$cityId" from database...""", "District")

    try {
      val sql = anorm.SQL("SELECT * FROM District WHERE cityId = {cityId} ORDER BY name").on("cityId" -> cityId)

      val districtList = Database.apply(sql) map {
        row =>
          val id: Int      = row[Int]("id")
          val name: String = row[String]("name")

          District(id, cityId, name)
      }

      Right(districtList)
    } catch {
      case t: Throwable =>
        Log.throwable(t, s"""Failed to get all districts for city "$cityId" from database!""", "District")
        Left(Errors(SingleError.Database.withDetails(s"""Failed to get all districts for city "$cityId" from database!""")))
    }
  }

  /**
   * Saves given districts to database
   *
   * @param districts Districts to save to database
   *
   * @return Non-empty errors if something goes wrong
   */
  def saveAllToDatabase(districts: List[District]): Errors = {
    try {
      if (districts.isEmpty) {
        Log.warn("Not saving empty list of districts...", "District")
        Errors.empty
      } else {
        Log.debug(s"""Saving all districts to database...""", "District")

        val valuesToParameters: List[(String, List[NamedParameter])] = districts.zipWithIndex.foldLeft(List.empty[(String, List[NamedParameter])]) {
          case (valuesToParameters: List[(String, List[NamedParameter])], (district: District, index: Int)) =>
            val idKey: String     = s"id$index"
            val cityIdKey: String = s"cityId$index"
            val nameKey: String   = s"name$index"

            valuesToParameters :+ {
              s"({$idKey}, {$cityIdKey}, {$nameKey})" -> List(
                NamedParameter(idKey,     district.id),
                NamedParameter(cityIdKey, district.cityId),
                NamedParameter(nameKey,   district.name)
              )
            }
        }

        val sql = anorm.SQL(
          s"""
             |INSERT INTO District (id, cityId, name)
             |VALUES ${valuesToParameters.map(_._1).mkString(", ")}
          """.stripMargin
        ).on(valuesToParameters.flatMap(_._2):_*)

        val savedCount = Database.executeUpdate(sql)

        if (savedCount != districts.size) {
          Log.error(s"""Failed to save ${districts.size} districts to database, affected row count was $savedCount!""", "District")
          Errors(SingleError.Database.withDetails("Failed to save some districts to database!"))
        } else {
          Errors.empty
        }
      }
    } catch {
      case t: Throwable =>
        Log.throwable(t, s"""Failed to save ${districts.size} districts to database!""", "District")
        Errors(SingleError.Database.withDetails("Failed to save all districts to database!"))
    }
  }
}
