package com.mehmetakiftutuncu.muezzinapi.utilities

import com.github.mehmetakiftutuncu.errors.base.ErrorBase
import com.github.mehmetakiftutuncu.errors.{CommonError, Errors, SimpleError}
import org.specs2.mutable.Specification
import play.api.libs.json.{JsObject, JsValue, Json}

class JsonErrorRepresenterSpec extends Specification {
  "Representing an ErrorBase" should {
    "represent SimpleError properly" in {
      val error: SimpleError = SimpleError("foo")
      val expectedResult: JsObject = Json.obj("name" -> "foo")

      val result: JsValue = JsonErrorRepresenter.represent(error, includeWhen = false)

      result mustEqual expectedResult
    }

    "represent CommonError with name properly" in {
      val error: CommonError = CommonError("foo")
      val expectedResult: JsObject = Json.obj("name" -> "foo")

      val result: JsValue = JsonErrorRepresenter.represent(error, includeWhen = false)

      result mustEqual expectedResult
    }

    "represent CommonError with name and reason properly" in {
      val error: CommonError = CommonError("foo", "bar")
      val expectedResult: JsObject = Json.obj("name" -> "foo", "reason" -> "bar")

      val result: JsValue = JsonErrorRepresenter.represent(error, includeWhen = false)

      result mustEqual expectedResult
    }

    "represent CommonError with name and data properly" in {
      val error: CommonError = CommonError("foo", data = "bar")
      val expectedResult: JsObject = Json.obj("name" -> "foo", "data" -> "bar")

      val result: JsValue = JsonErrorRepresenter.represent(error, includeWhen = false)

      result mustEqual expectedResult
    }

    "represent CommonError with name, reason and data properly" in {
      val error: CommonError = CommonError("foo", "bar", "baz")
      val expectedResult: JsObject = Json.obj("name" -> "foo", "reason" -> "bar", "data" -> "baz")

      val result: JsValue = JsonErrorRepresenter.represent(error, includeWhen = false)

      result mustEqual expectedResult
    }
  }

  br

  "Representing a representation as String" should {
    "represent a CommonError with name, reason and data as String properly" in {
      val error: CommonError = CommonError("foo", "bar", "baz")
      val expectedResult: String = Json.obj("name" -> "foo", "reason" -> "bar", "data" -> "baz").toString()

      val result: String = JsonErrorRepresenter.asString(JsonErrorRepresenter.represent(error, includeWhen = false))

      result mustEqual expectedResult
    }
  }

  br

  "Representing an ErrorBase list" should {
    "represent empty ErrorBase list properly" in {
      val errors: List[ErrorBase] = List.empty[ErrorBase]
      val expectedResult: JsValue = Json.arr()

      val result: JsValue = JsonErrorRepresenter.represent(errors, includeWhen = false)

      result mustEqual expectedResult
    }

    "represent non-empty ErrorBase list properly" in {
      val errors: List[ErrorBase] = List(SimpleError("foo"), CommonError("foo", "bar", "baz"))
      val expectedResult: JsValue = Json.arr(Json.obj("name" -> "foo"), Json.obj("name" -> "foo", "reason" -> "bar", "data" -> "baz"))

      val result: JsValue = JsonErrorRepresenter.represent(errors, includeWhen = false)

      result mustEqual expectedResult
    }
  }
}
