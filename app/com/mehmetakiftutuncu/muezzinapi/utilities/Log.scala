package com.mehmetakiftutuncu.muezzinapi.utilities

import com.github.mehmetakiftutuncu.errors.Errors
import play.api.Logger

object Log {
  def debug(message: => String)(implicit loggable: Logging): Unit                                       = Logger.debug(messageWithTag(message))
  def debug(message: => String, throwable: Throwable)(implicit loggable: Logging): Unit                 = Logger.debug(messageWithTag(message), throwable)
  def debug(message: => String, errors: Errors)(implicit loggable: Logging): Unit                       = Logger.debug(messageWithErrors(message, errors))
  def debug(message: => String, errors: Errors, throwable: Throwable)(implicit loggable: Logging): Unit = Logger.debug(messageWithErrors(message, errors), throwable)

  def warn(message: => String)(implicit loggable: Logging): Unit                                       = Logger.warn(messageWithTag(message))
  def warn(message: => String, throwable: Throwable)(implicit loggable: Logging): Unit                 = Logger.warn(messageWithTag(message), throwable)
  def warn(message: => String, errors: Errors)(implicit loggable: Logging): Unit                       = Logger.warn(messageWithErrors(message, errors))
  def warn(message: => String, errors: Errors, throwable: Throwable)(implicit loggable: Logging): Unit = Logger.warn(messageWithErrors(message, errors), throwable)

  def error(message: => String)(implicit loggable: Logging): Unit                                       = Logger.error(messageWithTag(message))
  def error(message: => String, throwable: Throwable)(implicit loggable: Logging): Unit                 = Logger.error(messageWithTag(message), throwable)
  def error(message: => String, errors: Errors)(implicit loggable: Logging): Unit                       = Logger.error(messageWithErrors(message, errors))
  def error(message: => String, errors: Errors, throwable: Throwable)(implicit loggable: Logging): Unit = Logger.error(messageWithErrors(message, errors), throwable)

  private def messageWithTag(message: => String)(implicit loggable: Logging): String = {
    s"[Muezzin - ${loggable.LOG_TAG}] $message"
  }

  private def messageWithErrors(message: => String, errors: Errors)(implicit loggable: Logging): String = {
    s"${messageWithTag(message)} Errors: ${errors.represent(JsonErrorRepresenter, includeWhen = false)}"
  }
}

trait Logging {
  self: Any =>

  implicit lazy val loggable: Logging = self

  lazy val LOG_TAG: String = self.getClass.getSimpleName.replaceAll("\\$", "")
}
