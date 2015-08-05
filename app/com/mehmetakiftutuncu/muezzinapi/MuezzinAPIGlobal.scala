package com.mehmetakiftutuncu.muezzinapi

import com.mehmetakiftutuncu.muezzinapi.utilities.Log
import play.api.mvc.RequestHeader
import play.api.mvc.Results._
import play.api.{Application, GlobalSettings}

import scala.concurrent.Future

object MuezzinAPIGlobal  extends GlobalSettings {
  override def onStart(app: Application) {
    Log.warn("Starting Muezzin API...", "Global")
  }

  override def onStop(app: Application) {
    Log.warn("Stopping Muezzin API...", "Global")
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound("Not found!"))
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful(BadRequest("Bad Request! " + error))
  }

  override def onError(request: RequestHeader, ex: Throwable) = {
    Future.successful(InternalServerError("An error occurred! " + ex.getMessage))
  }
}
