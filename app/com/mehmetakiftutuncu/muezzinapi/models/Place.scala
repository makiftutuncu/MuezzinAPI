package com.mehmetakiftutuncu.muezzinapi.models

case class Place(countryId: Int, cityId: Int, districtId: Option[Int]) {
  def toForm: Map[String, Seq[String]] = {
    Map(
      "Country" -> Seq(countryId.toString),
      "State"   -> Seq(cityId.toString),
      "period"  -> Seq("Aylik")
    ) ++ districtId.map(id => Map("City" -> Seq(id.toString))).getOrElse(Map.empty[String, Seq[String]])
  }

  def toLog: String = s"""country "$countryId", city "$cityId" and district "${districtId.map(_.toString).getOrElse("")}""""

  def toPath: String = s"country/$countryId/city/$cityId${districtId.map("/district/" + _.toString).getOrElse("")}"
}
