package com.mehmetakiftutuncu.muezzinapi.utilities

import com.github.mehmetakiftutuncu.errors.base.ErrorBase
import com.github.mehmetakiftutuncu.errors.representation.ErrorRepresenter
import com.github.mehmetakiftutuncu.errors.{CommonError, SimpleError}
import play.api.libs.json.{JsValue, Json}

object JsonErrorRepresenter extends ErrorRepresenter[JsValue] {
  override def represent(error: ErrorBase, includeWhen: Boolean): JsValue = {
    error match {
      case SimpleError(name)               => Json.obj("name" -> name)
      case CommonError(name, "", "")       => Json.obj("name" -> name)
      case CommonError(name, reason, "")   => Json.obj("name" -> name, "reason" -> reason)
      case CommonError(name, "", data)     => Json.obj("name" -> name, "data" -> data)
      case CommonError(name, reason, data) => Json.obj("name" -> name, "reason" -> reason, "data" -> data)
    }
  }

  override def asString(representation: JsValue): String = representation.toString()

  override def represent(errors: List[ErrorBase], includeWhen: Boolean): JsValue = Json.toJson(errors.map(represent(_, includeWhen)))
}
