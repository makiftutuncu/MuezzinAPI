package dev.akif.muezzinapi.location

import io.circe.{Encoder, Json}
import io.circe.syntax._

sealed trait Location {
  val id: Int
  val name: String
}

object Location {
  final case class Country(override val id: Int, override val name: String) extends Location

  final case class City(override val id: Int, override val name: String, country: Country) extends Location

  final case class District(override val id: Int, override val name: String, city: City) extends Location

  implicit val locationEncoder: Encoder[Location] =
    Encoder.instance[Location] { location =>
      Json.obj("id" := location.id, "name" := location.name)
    }
}
