package com.mehmetakiftutuncu.muezzinapi.models

case class Place(country: Country, city: City, district: Option[District]) {
  def toForm: Map[String, Seq[String]] = {
    Map(
      "Country" -> Seq(country.id.toString),
      "State"   -> Seq(city.id.toString),
      "period"  -> Seq("Aylik")
    ) ++ district.map(d => Map("City" -> Seq(d.id.toString))).getOrElse(Map.empty[String, Seq[String]])
  }

  def toLog: String = s"""country "${country.id}", city "${city.id}" and district "${district.map(_.id.toString).getOrElse("")}""""

  def toPath: String = s"country/${country.id}/city/${city.id}${district.map("/district/" + _.id.toString).getOrElse("")}"
}
