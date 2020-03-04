package dev.akif.muezzinapi

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

package object durum {
  type Req = HttpRequest
  val Req: HttpRequest.type = HttpRequest

  type Res = HttpResponse
  val Res: HttpResponse.type = HttpResponse
}
