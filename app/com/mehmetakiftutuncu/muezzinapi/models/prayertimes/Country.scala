package com.mehmetakiftutuncu.muezzinapi.models.prayertimes

import anorm.NamedParameter
import com.mehmetakiftutuncu.muezzinapi.models.base.{Databaseable, Jsonable, MuezzinAPIModel}
import com.mehmetakiftutuncu.muezzinapi.utilities.error.{Errors, SingleError}
import com.mehmetakiftutuncu.muezzinapi.utilities.{Database, Log}
import play.api.libs.json.{JsValue, Json}

case class Country(id: Int, name: String, trName: String) extends MuezzinAPIModel

object Country extends Jsonable[Country] with Databaseable[Country] {
  /**
   * Converts given object to Json
   *
   * @param country Object that will be converted to Json
   *
   * @return Json representation of given object
   */
  override def toJson(country: Country): JsValue = {
    Json.obj(
      "id"     -> country.id,
      "name"   -> country.name,
      "trName" -> country.trName
    )
  }

  /**
   * Tries to convert given Json to an object of current type
   *
   * @param json Json from which object will be generated
   *
   * @return Generated object or some errors
   */
  override def fromJson(json: JsValue): Either[Errors, Country] = {
    try {
      val id: Int        = (json \ "id").as[Int]
      val name: String   = (json \ "name").as[String]
      val trName: String = (json \ "trName").as[String]

      Right(Country(id, name, trName))
    } catch {
      case t: Throwable =>
        Log.throwable(t, s"""Failed to convert "$json" to a country!""", "Country")
        Left(Errors(SingleError.InvalidData.withValue(json.toString()).withDetails("Invalid country Json!")))
    }
  }

  /**
   * Gets all countries from database
   *
   * @return Some errors or a list of countries
   */
  override def getAllFromDatabase: Either[Errors, List[Country]] = {
    Log.debug(s"""Getting all countries from database...""")

    try {
      val sql = anorm.SQL("SELECT * FROM Country ORDER BY name")

      val countryList = Database.apply(sql) map {
        row =>
          val id: Int        = row[Int]("id")
          val name: String   = row[String]("name")
          val trName: String = row[String]("trName")

          Country(id, name, trName)
      }

      Right(countryList)
    } catch {
      case t: Throwable =>
        Log.throwable(t, s"""Failed to get all countries from database!""", "Country")
        Left(Errors(SingleError.Database.withDetails("Failed to get all countries from database!")))
    }
  }

  /**
   * Saves given countries to database
   *
   * @param countries Countries to save to database
   *
   * @return Non-empty errors if something goes wrong
   */
  override def saveAllToDatabase(countries: List[Country]): Errors = {
    Log.debug(s"""Saving all countries to database...""")

    try {
      val valuesToParameters: List[(String, List[NamedParameter])] = countries.zipWithIndex.foldLeft(List.empty[(String, List[NamedParameter])]) {
        case (valuesToParameters: List[(String, List[NamedParameter])], (country: Country, index: Int)) =>
          val idKey: String     = s"id$index"
          val nameKey: String   = s"name$index"
          val trNameKey: String = s"trName$index"

          valuesToParameters :+ {
            s"({$idKey}, {$nameKey}, {$trNameKey})" -> List(
              NamedParameter(idKey,     country.id),
              NamedParameter(nameKey,   country.name),
              NamedParameter(trNameKey, country.trName)
            )
          }
      }

      val sql = anorm.SQL(
        s"""
           |INSERT INTO Country (id, name, trName)
           |VALUES ${valuesToParameters.map(_._1).mkString(", ")}
       """.stripMargin
      ).on(valuesToParameters.flatMap(_._2):_*)

      val savedCount = Database.executeUpdate(sql)

      if (savedCount != countries.size) {
        Log.error(s"""Failed to save ${countries.size} countries to database, affected row count was $savedCount!""", "Country")
        Errors(SingleError.Database.withDetails("Failed to save some countries to database!"))
      } else {
        Errors.empty
      }
    } catch {
      case t: Throwable =>
        Log.throwable(t, s"""Failed to save ${countries.size} countries to database!""", "Country")
        Errors(SingleError.Database.withDetails("Failed to save all countries to database!"))
    }
  }
  
  val countryIdToTurkishNameMap: Map[Int, String] = Map(
    166 -> "Afganistan",
    13  -> "Almanya",
    33  -> "Amerika Birleşik Devletleri",
    17  -> "Andorra",
    140 -> "Angola",
    125 -> "Anguilla",
    90  -> "Antigua ve Barbuda",
    199 -> "Arjantin",
    25  -> "Arnavutluk",
    153 -> "Aruba",
    59  -> "Avustralya",
    35  -> "Avusturya",
    5   -> "Azerbaycan",
    54  -> "Bahamalar",
    132 -> "Bahreyn",
    177 -> "Bangladeş",
    188 -> "Barbados",
    11  -> "Belçika",
    182 -> "Belize",
    181 -> "Benin",
    51  -> "Bermuda",
    208 -> "Beyaz Rusya",
    93  -> "Birleşik Arap Emirlikleri",
    83  -> "Bolivya",
    9   -> "Bosna-Hersek",
    167 -> "Botsvana",
    146 -> "Brezilya",
    97  -> "Bruney",
    44  -> "Bulgaristan",
    91  -> "Burkina Faso",
    65  -> "Burundi",
    156 -> "Çad",
    16  -> "Çek Cumhuriyeti",
    86  -> "Cezayir",
    160 -> "Cibuti",
    61  -> "Çin (PRC)",
    26  -> "Danimarka",
    176 -> "Doğu Timor",
    72  -> "Dominik Cumhuriyeti",
    123 -> "Dominika",
    139 -> "Ekvador",
    63  -> "Ekvator Ginesi",
    165 -> "El Salvador",
    117 -> "Endonezya",
    175 -> "Eritre",
    104 -> "Ermenistan",
    6   -> "Estonya",
    95  -> "Etiyopya",
    145 -> "Fas",
    197 -> "Fiji",
    120 -> "Fildişi Sahili",
    126 -> "Filipinler",
    204 -> "Filistin",
    41  -> "Finlandiya",
    21  -> "Fransa",
    79  -> "Gabon",
    109 -> "Gambiya",
    143 -> "Gana",
    111 -> "Gine",
    58  -> "Grenada",
    48  -> "Grönland",
    171 -> "Guadeloupe",
    169 -> "Guam",
    99  -> "Guatemala",
    67  -> "Güney Afrika",
    128 -> "Güney Kore",
    62  -> "Gürcistan",
    70  -> "Haiti",
    187 -> "Hindistan",
    30  -> "Hırvatistan",
    4   -> "Hollanda",
    66  -> "Hollanda Antilleri",
    105 -> "Honduras",
    15  -> "İngiltere (Birleşik Krallık)",
    124 -> "Irak",
    202 -> "İran",
    32  -> "İrlanda",
    23  -> "İspanya",
    205 -> "İsrail",
    12  -> "İsveç",
    49  -> "İsviçre",
    8   -> "İtalya",
    122 -> "İzlanda",
    119 -> "Jamaika",
    116 -> "Japonya",
    161 -> "Kamboçya",
    184 -> "Kamerun",
    52  -> "Kanada",
    34  -> "Karadağ",
    94  -> "Katar",
    92  -> "Kazakistan",
    114 -> "Kenya",
    168 -> "Kırgızistan",
    1   -> "KKTC",
    57  -> "Kolombiya",
    88  -> "Komorlar",
    180 -> "Kongo",
    18  -> "Kosova",
    162 -> "Kosta Rika",
    209 -> "Küba",
    133 -> "Kuveyt",
    142 -> "Kuzey Kore",
    134 -> "Laos",
    174 -> "Lesotho",
    20  -> "Letonya",
    73  -> "Liberya",
    203 -> "Libya",
    38  -> "Lihtenştayn",
    47  -> "Litvanya",
    42  -> "Lübnan",
    31  -> "Lüksemburg",
    7   -> "Macaristan",
    98  -> "Madagaskar",
    28  -> "Makedonya",
    55  -> "Malavi",
    103 -> "Maldivler",
    107 -> "Malezya",
    152 -> "Mali",
    24  -> "Malta",
    87  -> "Martinique",
    164 -> "Mauritius",
    157 -> "Mayotte",
    53  -> "Meksika",
    189 -> "Mısır",
    60  -> "Moğolistan",
    46  -> "Moldova",
    3   -> "Monako",
    147 -> "Montserrat",
    106 -> "Moritanya",
    151 -> "Mozambik",
    154 -> "Myanmar",
    196 -> "Namibya",
    76  -> "Nepal",
    84  -> "Nijer",
    127 -> "Nijerya",
    178 -> "Niue",
    36  -> "Norveç",
    80  -> "Orta Afrika Cumhuriyeti",
    131 -> "Özbekistan",
    77  -> "Pakistan",
    149 -> "Palau",
    89  -> "Panama",
    185 -> "Papua Yeni Gine",
    194 -> "Paraguay",
    69  -> "Peru",
    183 -> "Pitcairn Adaları",
    39  -> "Polonya",
    45  -> "Portekiz",
    68  -> "Porto Riko",
    112 -> "Réunion",
    37  -> "Romanya",
    81  -> "Ruanda",
    207 -> "Rusya",
    198 -> "Samoa",
    102 -> "Senegal",
    138 -> "Seyşeller",
    200 -> "Şili",
    179 -> "Singapur",
    27  -> "Sırbistan",
    14  -> "Slovakya",
    19  -> "Slovenya",
    150 -> "Somali",
    74  -> "Sri Lanka",
    129 -> "Sudan",
    172 -> "Surinam",
    191 -> "Suriye",
    64  -> "Suudi Arabistan",
    163 -> "Svalbard",
    170 -> "Svaziland",
    101 -> "Tacikistan",
    110 -> "Tanzanya",
    137 -> "Tayland",
    108 -> "Tayvan",
    71  -> "Togo",
    130 -> "Tonga",
    96  -> "Trinidad ve Tobago",
    118 -> "Tunus",
    2   -> "Türkiye",
    159 -> "Türkmenistan",
    75  -> "Uganda",
    40  -> "Ukrayna",
    173 -> "Umman",
    192 -> "Ürdün",
    201 -> "Uruguay",
    56  -> "Vanuatu",
    10  -> "Vatikan",
    186 -> "Venezuela",
    135 -> "Vietnam",
    148 -> "Yemen",
    115 -> "Yeni Kaledonya",
    193 -> "Yeni Zelanda",
    144 -> "Yeşil Burun",
    22  -> "Yunanistan",
    158 -> "Zambiya",
    136 -> "Zimbabve"
  )

  val countryIdToNameMap: Map[Int, String] = Map(
    166 -> "Afghanistan [افغانستان]",
    13  -> "Germany [Deutschland]",
    33  -> "United States of America",
    17  -> "Andorra",
    140 -> "Angola",
    125 -> "Anguilla",
    90  -> "Antigua and Barbuda",
    199 -> "Argentina",
    25  -> "Albania [Shqipëria]",
    153 -> "Aruba",
    59  -> "Australia",
    35  -> "Austria [Österreich]",
    5   -> "Azerbaijan [Azərbaycan]",
    54  -> "Bahamas",
    132 -> "Bahrain [البحرين]",
    177 -> "Bangladesh [বাংলাদেশ]",
    188 -> "Barbados",
    11  -> "Belgium [België]",
    182 -> "Belize",
    181 -> "Benin [Bénin]",
    51  -> "Bermuda",
    208 -> "Belarus [Беларусь]",
    93  -> "United Arab Emirates [الإمارات العربيّة المتّحدة]",
    83  -> "Bolivia [Buliwya]",
    9   -> "Bosnia Herzegovina [Босна и Херцеговина]",
    167 -> "Botswana",
    146 -> "Brazil [Brasil]",
    97  -> "Brunei [بروني]",
    44  -> "Bulgaria [България]",
    91  -> "Burkina Faso",
    65  -> "Burundi",
    156 -> "Chad [تشاد]",
    16  -> "Czech Republic [Česká republika]",
    86  -> "Algeria [الجزائر]",
    160 -> "Djibouti [جيبوتي]",
    61  -> "People's Republic of China (PRC) [中国 (中华人民共和国)]",
    26  -> "Denmark [Danmark]",
    176 -> "East Timor [Timor Lorosa'e]",
    72  -> "Dominican Republic [República Dominicana]",
    123 -> "Dominica",
    139 -> "Ecuador",
    63  -> "Equatorial Guinea [Guinea Ecuatorial]",
    165 -> "El Salvador",
    117 -> "Indonesia",
    175 -> "Eritrea [إرتريا]",
    104 -> "Armenia [Հայաստան]",
    6   -> "Estonia [Eesti]",
    95  -> "Ethiopia [ኢትዮጲያ]",
    145 -> "Morocco [المغرب]",
    197 -> "Fiji",
    120 -> "Côte d'Ivoire",
    126 -> "Philippines [Pilipinas]",
    204 -> "Palestine [فلسطين]",
    41  -> "Finland [Suomi]",
    21  -> "France",
    79  -> "Gabon",
    109 -> "Gambia",
    143 -> "Ghana",
    111 -> "Guinea [Guinée]",
    58  -> "Grenada",
    48  -> "Greenland [Grønland]",
    171 -> "Guadeloupe",
    169 -> "Guam",
    99  -> "Guatemala",
    67  -> "South Africa",
    128 -> "South Korea [한국 / 韓國]",
    62  -> "Georgia [საქართველო]",
    70  -> "Haiti [Haïti]",
    187 -> "India",
    30  -> "Croatia [Hrvatska]",
    4   -> "Netherlands [Nederland]",
    66  -> "Netherlands Antilles [Nederlandse Antillen]",
    105 -> "Honduras",
    15  -> "England [United Kingdom]",
    124 -> "Iraq [العراق]",
    202 -> "Iran [ایران]",
    32  -> "Republic of Ireland [Éire]",
    23  -> "Spain [España]",
    205 -> "Israel [ישראל]",
    12  -> "Sweden [Sverige]",
    49  -> "Switzerland [Schweiz]",
    8   -> "Italy [Italia]",
    122 -> "Iceland [Ísland]",
    119 -> "Jamaica",
    116 -> "Japan [日本]",
    161 -> "Cambodia [Kampuchea]",
    184 -> "Cameroon",
    52  -> "Canada",
    34  -> "Montenegro [Црна Гора]",
    94  -> "Qatar [قطر]",
    92  -> "Kazakhstan [Қазақстан]",
    114 -> "Kenya",
    168 -> "Kyrgyzstan [Кыргызстан]",
    1   -> "Cyprus [Kıbrıs]",
    57  -> "Colombia",
    88  -> "Comoros [Komori]",
    180 -> "Republic of Congo",
    18  -> "Kosovo [Косово]",
    162 -> "Costa Rica",
    209 -> "Cuba",
    133 -> "Kuwait [الكويت]",
    142 -> "North Korea [조선 / 朝鮮]",
    134 -> "Laos [ລາວ]",
    174 -> "Lesotho",
    20  -> "Latvia [Latvija]",
    73  -> "Liberia",
    203 -> "Libya [ليبيا]",
    38  -> "Liechtenstein",
    47  -> "Lithuania [Lietuva]",
    42  -> "Lebanon [لبنان]",
    31  -> "Luxembourg [Lëtzebuerg]",
    7   -> "Hungary [Magyarország]",
    98  -> "Madagascar [Madagasikara]",
    28  -> "Republic of Macedonia [Македонија]",
    55  -> "Malawi",
    103 -> "Maldives [Dhivehi Raajje]",
    107 -> "Malaysia",
    152 -> "Mali",
    24  -> "Malta",
    87  -> "Martinique",
    164 -> "Mauritius [Maurice]",
    157 -> "Mayotte",
    53  -> "Mexico [México]",
    189 -> "Egypt [مصر]",
    60  -> "Mongolia [Монгол Улс]",
    46  -> "Moldova",
    3   -> "Monaco",
    147 -> "Montserrat",
    106 -> "Mauritania [Mauritanie]",
    151 -> "Mozambique [Moçambique]",
    154 -> "Myanmar [Myanma]",
    196 -> "Namibia",
    76  -> "Nepal [नेपाल]",
    84  -> "Niger",
    127 -> "Nigeria",
    178 -> "Niue",
    36  -> "Norway [Norge]",
    80  -> "Central African Republic [République Centrafricaine]",
    131 -> "Uzbekistan [O'zbekiston]",
    77  -> "Pakistan [پاکستان]",
    149 -> "Palau [Belau]",
    89  -> "Panama [Panamá]",
    185 -> "Papua New Guinea",
    194 -> "Paraguay",
    69  -> "Peru [Perú]",
    183 -> "Pitcairn Islands",
    39  -> "Poland [Polska]",
    45  -> "Portugal",
    68  -> "Puerto Rico",
    112 -> "Réunion",
    37  -> "Romania",
    81  -> "Rwanda",
    207 -> "Russia [Россия]",
    198 -> "Samoa",
    102 -> "Senegal [Sénégal]",
    138 -> "Seychelles [Sesel]",
    200 -> "Chile",
    179 -> "Singapore [Singapura]",
    27  -> "Serbia [Србија]",
    14  -> "Slovakia [Slovensko]",
    19  -> "Slovenia [Slovenija]",
    150 -> "Somalia [Soomaaliya]",
    74  -> "Sri Lanka [Sri Lankā]",
    129 -> "Sudan [السودان]",
    172 -> "Suriname",
    191 -> "Syria] [سورية]",
    64  -> "Saudi Arabia",
    163 -> "Svalbard",
    170 -> "Swaziland",
    101 -> "Tajikistan [Тоҷикистон]",
    110 -> "Tanzania",
    137 -> "Thailand [Mueang Thai]",
    108 -> "Taiwan [中華民國]",
    71  -> "Togo",
    130 -> "Tonga",
    96  -> "Trinidad and Tobago",
    118 -> "Tunisia [تونس]",
    2   -> "Turkey [Türkiye]",
    159 -> "Turkmenistan [Türkmenistan]",
    75  -> "Uganda",
    40  -> "Ukraine [Україна]",
    173 -> "Oman [عُمان]",
    192 -> "Jordan [الأردن]",
    201 -> "Uruguay [República Oriental del Uruguay]",
    56  -> "Vanuatu",
    10  -> "Vatican City [Civitas Vaticana]",
    186 -> "Venezuela",
    135 -> "Vietnam [Việt Nam]",
    148 -> "Yemen [اليمن]",
    115 -> "New Caledonia [Nouvelle-Calédonie]",
    193 -> "New Zealand",
    144 -> "Cape Verde [Cabo Verde]",
    22  -> "Greece [Ελλάδα]",
    158 -> "Zambia",
    136 -> "Zimbabwe"
  )
}
