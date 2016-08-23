package com.mehmetakiftutuncu.muezzinapi.utilities

import com.github.mehmetakiftutuncu.errors.Errors
import play.api.http.ContentTypes
import play.api.libs.json.JsValue
import play.api.mvc.{Controller, Result}

import scala.concurrent.Future

trait ControllerBase extends Controller with Logging {
  protected def success(result: JsValue): Result = {
    Ok(result).as(ContentTypes.JSON)
  }

  protected def futureSuccess(result: JsValue): Future[Result] = {
    Future.successful(success(result))
  }

  protected def failWithErrors(errors: Errors): Result = {
    ServiceUnavailable(errors.represent(JsonErrorRepresenter, includeWhen = true)).as(ContentTypes.JSON)
  }

  protected def failWithErrors(message: => String, errors: Errors): Result = {
    Log.error(message, errors)

    failWithErrors(errors)
  }

  protected def futureFailWithErrors(errors: Errors): Future[Result] = {
    Future.successful(failWithErrors(errors))
  }

  protected def futureFailWithErrors(message: => String, errors: Errors): Future[Result] = {
    Log.error(message, errors)

    futureFailWithErrors(errors)
  }
}
