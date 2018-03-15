package com.mehmetakiftutuncu.muezzinapi.utilities

import java.util.concurrent.TimeUnit

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment}

import scala.concurrent.duration.{Duration, FiniteDuration}

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
    configuration.getOptional[Boolean](path).getOrElse(defaultValue)
  }

  override def getString(path: String): Option[String] = {
    configuration.getOptional[String](path)
  }

  override def getString(path: String, defaultValue: String): String = {
    getString(path).getOrElse(defaultValue)
  }

  override def getFiniteDuration(path: String): Option[FiniteDuration] = {
    configuration.getOptional[Duration](path).map(d => FiniteDuration(d.toMillis, TimeUnit.MILLISECONDS))
  }

  override def getFiniteDuration(path: String, defaultValue: FiniteDuration): FiniteDuration = {
    getFiniteDuration(path).getOrElse(defaultValue)
  }
}
