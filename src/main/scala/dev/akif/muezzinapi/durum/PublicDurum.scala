package dev.akif.muezzinapi.durum

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import dev.akif.durum.{Durum, LogType, OutputBuilder, RequestLog, ResponseLog}
import dev.akif.muezzinapi.durum.implicits._
import e.scala.E
import e.zio.EIO

object PublicDurum extends Durum[EIO, E, Req, Res, Unit, PublicCtx] {
  override val errorOutputBuilder: OutputBuilder[EIO, E, Res] = eioEOutputBuilder

  override def getHeadersOfRequest(request: Req): Map[String, String] = akkaHeadersToMap(request.headers)

  override def getMethodOfRequest(request: Req): String = request.method.name

  override def getURIOfRequest(request: Req): String = request.uri.toString

  override def buildAuth(request: Req): EIO[Unit] = EIO.unit

  override def buildContext[IN](id: String,
                                time: Long,
                                request: Req,
                                headers: Map[String, String],
                                in: IN,
                                auth: Unit): PublicCtx[IN] =
    PublicCtx[IN](id, time, request, headers, in)

  override def getStatusOfResponse(response: Res): Int = response.status.intValue()

  override def getStatusOfError(e: E): Int = e.code

  override def responseWithHeader(response: Res, header: (String, String)): Res =
    HttpHeader.parse(header._1, header._2) match {
      case ParsingResult.Error(_)      => response
      case ParsingResult.Ok(header, _) => response.withHeaders(header)
    }

  override def getHeadersOfResponse(response: Res): Map[String, String] = akkaHeadersToMap(response.headers)

  override def logRequest(log: RequestLog): Unit = println(log.toLog(LogType.IncomingRequest))

  override def logResponse(log: ResponseLog): Unit = println(log.toLog(LogType.OutgoingResponse))

  private def akkaHeadersToMap(headers: Seq[HttpHeader]): Map[String, String] =
    headers.foldLeft(Map.empty[String, String]) {
      case (map, header) => map + (header.name -> header.value)
    }
}
