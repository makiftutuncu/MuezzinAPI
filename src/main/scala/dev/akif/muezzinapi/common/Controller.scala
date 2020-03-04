package dev.akif.muezzinapi.common

import akka.http.scaladsl.server.{Route, RouteResult}
import dev.akif.muezzinapi.durum.implicits.eHttpResponse
import dev.akif.muezzinapi.durum.Res
import e.zio.EIO
import zio.Runtime

import scala.concurrent.{Future, Promise}
import scala.language.implicitConversions

abstract class Controller(val root: String)(implicit rt: Runtime[Any]) {
  val route: Route

  implicit def durumRoute(eio: EIO[Res]): Future[RouteResult] = {
    val promise: Promise[RouteResult] = Promise()
    rt.unsafeRunAsync(eio) {
      _.fold(
        _.failureOrCause.fold(
          e => promise.success(RouteResult.Complete(eHttpResponse(e))),
          cause => promise.success(RouteResult.Complete(eHttpResponse(Errors.unexpected.cause(cause.squash))))
        ),
        res => promise.success(RouteResult.Complete(res))
      )
    }
    promise.future
  }
}
