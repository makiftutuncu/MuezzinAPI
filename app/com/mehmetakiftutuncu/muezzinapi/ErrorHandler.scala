package com.mehmetakiftutuncu.muezzinapi

import com.github.mehmetakiftutuncu.errors.{CommonError, Errors}
import com.google.inject.Provider
import com.mehmetakiftutuncu.muezzinapi.utilities.{ControllerExtras, Log}
import javax.inject.{Inject, Singleton}
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.{RequestHeader, Result}
import play.api.routing.Router
import play.api.{Configuration, Environment, OptionalSourceMapper, UsefulException}

import scala.concurrent.Future

@Singleton
class ErrorHandler @Inject()(Environment: Environment,
                             Config: Configuration,
                             SourceMapper: OptionalSourceMapper,
                             Router: Provider[Router]) extends DefaultHttpErrorHandler(Environment, Config, SourceMapper, Router) with ControllerExtras {
  override protected def onProdServerError(request: RequestHeader, exception: UsefulException): Future[Result] = {
    val errors: Errors = Errors(CommonError.requestFailed.reason(exception.getMessage))

    Log.error(s"""Request "$request" failed with exception!""", errors, exception)

    futureFailWithErrors(errors)
  }

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    futureFailWithErrors(s"""Request "$request" failed with status code "$statusCode"!""", Errors(CommonError.requestFailed.reason(message).data(statusCode.toString)))
  }

  override protected def onOtherClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = onClientError(request, statusCode, message)
}
