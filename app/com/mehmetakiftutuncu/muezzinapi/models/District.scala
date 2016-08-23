package com.mehmetakiftutuncu.muezzinapi.models

import play.api.libs.json.{JsObject, Json}

case class District(id: Int, cityId: Int, name: String) {
  def toJson: JsObject = Json.obj(id.toString -> name)
}
