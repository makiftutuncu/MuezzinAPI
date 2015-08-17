package com.mehmetakiftutuncu.muezzinapi.models.prayertimes

import anorm.NamedParameter
import com.mehmetakiftutuncu.muezzinapi.models.base.Jsonable
import com.mehmetakiftutuncu.muezzinapi.utilities.error.{Errors, SingleError}
import com.mehmetakiftutuncu.muezzinapi.utilities.{Database, Log}
import play.api.libs.json.{JsValue, Json}

/**
 * Represents a city in a country
 *
 * @param id        Id of the city as a number
 * @param countryId Id of country this city belongs to
 * @param name      Name of the city
 */
case class City(id: Int, countryId: Int, name: String) extends Jsonable[City] {
  /**
   * Converts this object to Json
   *
   * @return Json representation of this object
   */
  override def toJson: JsValue = Json.obj("id" -> id, "name" -> name)
}

/**
 * Companion object of City
 */
object City {
  /**
   * Gets all cities from database
   *
   * @param countryId Id of the country for which to get cities
   *
   * @return Some errors or a list of cities
   */
  def getAllFromDatabase(countryId: Int): Either[Errors, List[City]] = {
    Log.debug(s"""Getting all cities for country "$countryId" from database...""", "City")

    try {
      val sql = anorm.SQL("SELECT * FROM City WHERE countryId = {countryId} ORDER BY name").on("countryId" -> countryId)

      val cityList = Database.apply(sql) map {
        row =>
          val id: Int      = row[Int]("id")
          val name: String = row[String]("name")

          City(id, countryId, name)
      }

      Right(cityList)
    } catch {
      case t: Throwable =>
        Log.throwable(t, s"""Failed to get all cities for country "$countryId" from database!""", "City")
        Left(Errors(SingleError.Database.withDetails(s"""Failed to get all cities for country "$countryId" from database!""")))
    }
  }

  /**
   * Saves given cities to database
   *
   * @param cities Cities to save to database
   *
   * @return Non-empty errors if something goes wrong
   */
  def saveAllToDatabase(cities: List[City]): Errors = {
    try {
      if (cities.isEmpty) {
        Log.warn("Not saving empty list of cities...", "City")
        Errors.empty
      } else {
        Log.debug(s"""Saving all cities to database...""", "City")

        val valuesToParameters: List[(String, List[NamedParameter])] = cities.zipWithIndex.foldLeft(List.empty[(String, List[NamedParameter])]) {
          case (valuesToParameters: List[(String, List[NamedParameter])], (city: City, index: Int)) =>
            val idKey: String        = s"id$index"
            val countryIdKey: String = s"countryId$index"
            val nameKey: String      = s"name$index"

            valuesToParameters :+ {
              s"({$idKey}, {$countryIdKey}, {$nameKey})" -> List(
                NamedParameter(idKey,        city.id),
                NamedParameter(countryIdKey, city.countryId),
                NamedParameter(nameKey,      city.name)
              )
            }
        }

        val sql = anorm.SQL(
          s"""
             |INSERT INTO City (id, countryId, name)
             |VALUES ${valuesToParameters.map(_._1).mkString(", ")}
           """.stripMargin
          ).on(valuesToParameters.flatMap(_._2):_*)

        val savedCount = Database.executeUpdate(sql)

        if (savedCount != cities.size) {
          Log.error(s"""Failed to save ${cities.size} cities to database, affected row count was $savedCount!""", "City")
          Errors(SingleError.Database.withDetails("Failed to save some cities to database!"))
        } else {
          Errors.empty
        }
      }
    } catch {
      case t: Throwable =>
        Log.throwable(t, s"""Failed to save ${cities.size} cities to database!""", "City")
        Errors(SingleError.Database.withDetails("Failed to save all cities to database!"))
    }
  }

  /** A mapping of city ids of Diyanet to their correctly spelled names, unfortunatelly only for Turkey */
  val cityIdToTurkishNameMap: Map[Int, String] = Map(
    500 -> "Adana",
    501 -> "Adıyaman",
    502 -> "Afyon",
    504 -> "Aksaray",
    505 -> "Amasya",
    506 -> "Ankara",
    507 -> "Antalya",
    508 -> "Ardahan",
    509 -> "Artvin",
    510 -> "Aydın",
    503 -> "Ağrı",
    511 -> "Balıkesir",
    512 -> "Bartın",
    513 -> "Batman",
    514 -> "Bayburt",
    515 -> "Bilecik",
    516 -> "Bingöl",
    517 -> "Bitlis",
    518 -> "Bolu",
    519 -> "Burdur",
    520 -> "Bursa",
    521 -> "Çanakkale",
    522 -> "Çankırı",
    523 -> "Çorum",
    524 -> "Denizli",
    525 -> "Diyarbakır",
    526 -> "Düzce",
    527 -> "Edirne",
    528 -> "Elazığ",
    529 -> "Erzincan",
    530 -> "Erzurum",
    531 -> "Eskişehir",
    532 -> "Gaziantep",
    533 -> "Giresun",
    534 -> "Gümüşhane",
    535 -> "Hakkari",
    536 -> "Hatay",
    538 -> "Isparta",
    539 -> "İstanbul",
    540 -> "İzmir",
    537 -> "Iğdır",
    541 -> "Kahramanmaraş",
    542 -> "Karabük",
    543 -> "Karaman",
    544 -> "Kars",
    545 -> "Kastamonu",
    546 -> "Kayseri",
    547 -> "Kilis",
    548 -> "Kırıkkale",
    549 -> "Kırklareli",
    550 -> "Kırşehir",
    551 -> "Kocaeli",
    552 -> "Konya",
    553 -> "Kütahya",
    554 -> "Malatya",
    555 -> "Manisa",
    556 -> "Mardin",
    557 -> "Mersin",
    558 -> "Muğla",
    559 -> "Muş",
    560 -> "Nevşehir",
    561 -> "Niğde",
    562 -> "Ordu",
    563 -> "Osmaniye",
    564 -> "Rize",
    565 -> "Sakarya",
    566 -> "Samsun",
    568 -> "Siirt",
    569 -> "Sinop",
    571 -> "Sivas",
    567 -> "Şanlıurfa",
    570 -> "Şırnak",
    572 -> "Tekirdağ",
    573 -> "Tokat",
    574 -> "Trabzon",
    575 -> "Tunceli",
    576 -> "Uşak",
    577 -> "Van",
    578 -> "Yalova",
    579 -> "Yozgat",
    580 -> "Zonguldak"
  )
}
