package com.mehmetakiftutuncu.muezzinapi.utilities

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import play.api.{Configuration, Environment}

import scala.concurrent.duration.FiniteDuration

@ImplementedBy(classOf[Conf])
trait AbstractConf {
  def getBoolean(path: String, defaultValue: Boolean): Boolean
  def getString(path: String): Option[String]
  def getString(path: String, defaultValue: String): String
  def getFiniteDuration(path: String): Option[FiniteDuration]
  def getFiniteDuration(path: String, defaultValue: FiniteDuration): FiniteDuration
}

@Singleton
class Conf @Inject()(environment: Environment) extends AbstractConf {
  private val configuration: Configuration = Configuration.load(environment)

  override def getBoolean(path: String, defaultValue: Boolean): Boolean = {
    configuration.getBoolean(path).getOrElse(defaultValue)
  }

  override def getString(path: String): Option[String] = {
    configuration.getString(path)
  }

  override def getString(path: String, defaultValue: String): String = {
    getString(path).getOrElse(defaultValue)
  }

  override def getFiniteDuration(path: String): Option[FiniteDuration] = {
    configuration.getMilliseconds(path).map(FiniteDuration(_, TimeUnit.MILLISECONDS))
  }

  override def getFiniteDuration(path: String, defaultValue: FiniteDuration): FiniteDuration = {
    getFiniteDuration(path).getOrElse(defaultValue)
  }
}
