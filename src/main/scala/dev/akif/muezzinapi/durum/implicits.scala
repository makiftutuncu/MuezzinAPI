package dev.akif.muezzinapi.durum

import akka.http.scaladsl.model._
import dev.akif.durum.{Effect, OutputBuilder}
import e.circe.implicits._
import e.scala.E
import e.zio.EIO
import e.zio.syntax._
import io.circe.Encoder
import io.circe.syntax._

import scala.language.implicitConversions

object implicits {
  implicit val eioEffect: Effect[EIO, E] = new Effect[EIO, E] {
    override def pure[A](a: A): EIO[A] = a.toEIO

    override def error[A](e: E): EIO[A] = e.toEIO

    override def map[A, B](f: EIO[A])(m: A => B): EIO[B] = f.map(m)

    override def flatMap[A, B](f: EIO[A])(fm: A => EIO[B]): EIO[B] = f.flatMap(fm)

    override def foreach[A, U](f: EIO[A])(fe: A => U): Unit = f.map(fe)

    override def fold[A, B](f: EIO[A])(handleError: E => EIO[B], fm: A => EIO[B]): EIO[B] = f.foldM(handleError, fm)

    override def mapError[A, AA >: A](f: EIO[A])(handleError: E => EIO[AA]): EIO[AA] = f.foldM(handleError, pure)
  }

  implicit def eHttpResponse(e: E): HttpResponse = jsonHttpResponse[E](e, e.code)

  implicit def jsonHttpResponse[A: Encoder](a: A, status: Int = StatusCodes.OK.intValue): HttpResponse =
    Res(
      status,
      entity = HttpEntity.apply(ContentTypes.`application/json`, a.asJson.noSpaces)
    )

  implicit val eioEOutputBuilder: OutputBuilder[EIO, E, Res] =
    new OutputBuilder[EIO, E, Res] {
      override def build(status: Int, e: E): EIO[Res] = eHttpResponse(e).toEIO

      override def log(e: E): EIO[String] = e.toString.toEIO
    }

  implicit def eioJsonOutputBuilder[A: Encoder]: OutputBuilder[EIO, A, Res] =
    new OutputBuilder[EIO, A, Res] {
      override def build(status: Int, a: A): EIO[Res] = jsonHttpResponse(a, status).toEIO

      override def log(a: A): EIO[String] = a.asJson.noSpaces.toEIO
    }
}
