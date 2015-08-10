package com.mehmetakiftutuncu.muezzinapi.models.base

import com.mehmetakiftutuncu.muezzinapi.utilities.error.Errors
import play.api.http.ContentTypes
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Codec, Controller, Result}

import scala.concurrent.Future

/** A base trait for Muezzin API controllers */
trait MuezzinAPIController extends Controller {
  /**
   * Generates a response with Json data
   *
   * @param result Response Json data
   *
   * @return A future response with given data
   */
  def futureJsonResponse(result: JsValue): Future[Result] = {
    Future.successful(jsonResponse(result))
  }

  /**
   * Generates a response with Json data
   *
   * @param result Response Json data
   *
   * @return A response with given data
   */
  def jsonResponse(result: JsValue): Result = {
    Ok(result).as(ContentTypes.JSON(Codec.utf_8))
  }

  /**
   * Generates a response with errors
   *
   * @param errors Response errors
   *
   * @return A future response with given errors
   */
  def futureErrorResponse(errors: Errors): Future[Result] = {
    Future.successful(errorResponse(errors))
  }

  /**
   * Generates a response with errors
   *
   * @param errors Response errors
   *
   * @return A response with given errors
   */
  def errorResponse(errors: Errors): Result = {
    InternalServerError(Json.obj("errors" -> errors.toJson)).as(ContentTypes.JSON(Codec.utf_8))
  }
}
