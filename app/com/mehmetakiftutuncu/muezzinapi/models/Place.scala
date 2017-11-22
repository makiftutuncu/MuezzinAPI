package com.mehmetakiftutuncu.muezzinapi.models

case class Place(countryId: Int, cityId: Option[Int], districtId: Option[Int]) {
  def toLog: String = s"""country "$countryId", city "${cityId.map(_.toString).getOrElse("")}" and district "${districtId.map(_.toString).getOrElse("")}""""

  def toPath: String = s"country/$countryId${cityId.map("/city/" + _).getOrElse("")}${districtId.map("/district/" + _).getOrElse("")}"

  def toKey: String = s"$countryId${cityId.map("." + _).getOrElse("")}${districtId.map("." + _).getOrElse("")}"
}
