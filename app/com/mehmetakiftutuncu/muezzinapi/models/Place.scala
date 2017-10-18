package com.mehmetakiftutuncu.muezzinapi.models

case class Place(countryId: Int, cityId: Option[Int], districtId: Option[Int]) {
  def toForm: Map[String, Seq[String]] = {
    val countryMap: Map[String, Seq[String]]  = Map("ulkeId" -> Seq(countryId.toString))
    val cityMap: Map[String, Seq[String]]     = cityId.map(id => Map("ilId" -> Seq(id.toString))).getOrElse(Map.empty)
    val districtMap: Map[String, Seq[String]] = districtId.map(id => Map("ilceId" -> Seq(id.toString))).getOrElse(Map.empty)

    countryMap ++ cityMap ++ districtMap
  }

  def toLog: String = s"""country "$countryId", city "${cityId.map(_.toString).getOrElse("")}" and district "${districtId.map(_.toString).getOrElse("")}""""

  def toPath: String = s"country/$countryId${cityId.map("/city/" + _).getOrElse("")}${districtId.map("/district/" + _).getOrElse("")}"

  def toKey: String = s"$countryId${cityId.map("." + _).getOrElse("")}${districtId.map("." + _).getOrElse("")}"
}
