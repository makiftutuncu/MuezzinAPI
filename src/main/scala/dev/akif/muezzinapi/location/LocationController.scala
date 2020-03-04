package dev.akif.muezzinapi.location

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import dev.akif.muezzinapi.common.{Controller, Errors}
import dev.akif.muezzinapi.durum.implicits._
import dev.akif.muezzinapi.durum.{EIO, PublicCtx, PublicDurum}
import zio.Runtime

class LocationController(implicit rt: Runtime[Any]) extends Controller("location") {
  override val route: Route =
    path(root) {
      get { request =>
        PublicDurum.wrapWithOutput(request.request) { publicCtx: PublicCtx[Unit] =>
          getLocations
        }
      }
    }

  def getLocations: EIO[List[Location]] = EIO.effectTotal(List.empty)
}
