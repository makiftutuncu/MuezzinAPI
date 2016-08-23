package com.mehmetakiftutuncu.muezzinapi.utilities

import play.api.mvc.PathBindable

import scala.util.{Failure, Success, Try}

object OptionIntPathBindable {
  implicit def optionIntPathBindable(implicit binder: PathBindable[Int]): PathBindable[Option[Int]] = new PathBindable[Option[Int]] {
    override def bind(key: String, value: String): Either[String, Option[Int]] = {
      val trimmedValue: String = Option(value).getOrElse("").trim

      if (trimmedValue.isEmpty) {
        Right(Option.empty[Int])
      } else {
        Try(trimmedValue.toInt) match {
          case Success(int) => Right(Option(int))
          case Failure(t)   => Left(s"Failed to parse '$value' as Option[Int]")
        }
      }
    }

    override def unbind(key: String, value: Option[Int]): String = {
      value.map(binder.unbind(key, _)).getOrElse("")
    }
  }
}
