package com.mehmetakiftutuncu.muezzinapi.utilities

import org.joda.time.DateTime
import play.api.Logger

/**
 * A utility object for logging stuff
 */
object Log {
  /**
   * Log at debug level
   *
   * @param message Message to log
   * @param tag     An optional tag for the log
   */
  def debug(message: => String, tag: => String = ""): Unit = {
    Logger.debug(format(tag, message))
  }

  /**
   * Log at warn level
   *
   * @param message Message to log
   * @param tag     An optional tag for the log
   */
  def warn(message: => String, tag: => String = ""): Unit = {
    Logger.warn(format(tag, message))
  }

  /**
   * Logs at error level
   *
   * @param message Message to log
   * @param tag     An optional tag for the log
   */
  def error(message: => String, tag: => String = ""): Unit = {
    Logger.error(format(tag, message))
  }

  /**
   * Logs at error level with a throwable
   *
   * @param error   An error to log details of error
   * @param message Message to log
   * @param tag     An optional tag for the log
   */
  def throwable(error: Throwable, message: => String, tag: => String = ""): Unit = {
    Logger.error(format(tag, message), error)
  }

  private def format(tag: => String, message: => String): String = {
    val now = DateTime.now().toString("YY.MM.dd, HH:mm:ss")

    s"[$now]${if (tag.nonEmpty) s" [$tag]" else ""} $message"
  }
}
