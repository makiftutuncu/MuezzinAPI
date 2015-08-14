package com.mehmetakiftutuncu.muezzinapi.models.prayertimes

import com.mehmetakiftutuncu.muezzinapi.models.base.{Jsonable, MuezzinAPIModel}
import com.mehmetakiftutuncu.muezzinapi.utilities.Log
import com.mehmetakiftutuncu.muezzinapi.utilities.error.{Errors, SingleError}
import play.api.libs.json.{JsValue, Json}

case class City(id: Int, name: String) extends MuezzinAPIModel

object City extends Jsonable[City] {
  /**
   * Converts given object to Json
   *
   * @param city Object that will be converted to Json
   *
   * @return Json representation of given object
   */
  override def toJson(city: City): JsValue = {
    Json.obj(
      "id"   -> city.id,
      "name" -> city.name
    )
  }

  /**
   * Tries to convert given Json to an object of current type
   *
   * @param json Json from which object will be generated
   *
   * @return Generated object or some errors
   */
  override def fromJson(json: JsValue): Either[Errors, City] = {
    try {
      val id: Int      = (json \ "id").as[Int]
      val name: String = (json \ "name").as[String]

      Right(City(id, name))
    } catch {
      case t: Throwable =>
        Log.throwable(t, s"""Failed to convert "$json" to a city!""", "City")
        Left(Errors(SingleError.InvalidData.withValue(json.toString()).withDetails("Invalid city Json!")))
    }
  }

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
