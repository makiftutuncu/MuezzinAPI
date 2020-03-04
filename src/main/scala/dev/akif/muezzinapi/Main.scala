package dev.akif.muezzinapi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, Materializer}
import dev.akif.muezzinapi.common.Controller
import dev.akif.muezzinapi.location.LocationController
import zio.Runtime
import zio.internal.Platform

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object Main {
  implicit val actorSystem: ActorSystem           = ActorSystem("muezzin-api")
  implicit val materializer: Materializer         = ActorMaterializer()
  implicit val executionContext: ExecutionContext = actorSystem.dispatcher
  implicit val runtime: Runtime[Any]              = Runtime((), Platform.fromExecutionContext(executionContext))

  val controllers: List[Controller] =
    List(
      new LocationController
    )

  val routes: Route = controllers.foldLeft[Route](reject) { case (r: Route, c: Controller) => concat(c.route, r) }

  def main(args: Array[String]): Unit = {
    val bindingFuture = Http().bindAndHandle(routes, "0.0.0.0", 8080)
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => actorSystem.terminate())
  }
}
