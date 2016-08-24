package com.mehmetakiftutuncu.muezzinapi.data

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import com.mehmetakiftutuncu.muezzinapi.utilities.{AbstractConf, Log, Logging}
import play.api.cache.CacheApi

import scala.concurrent.duration.FiniteDuration
import scala.reflect.ClassTag

@ImplementedBy(classOf[Cache])
trait AbstractCache {
  protected val timeout: FiniteDuration

  def get[T: ClassTag](key: String): Option[T]
  def set[T: ClassTag](key: String, value: T, timeout: FiniteDuration = timeout): Unit
  def remove(key: String): Unit
}

@Singleton
class Cache @Inject()(CacheApi: CacheApi, Conf: AbstractConf) extends AbstractCache with Logging {
  override protected val timeout: FiniteDuration = Conf.getFiniteDuration("muezzinApi.cache.timeout", FiniteDuration(1, TimeUnit.DAYS))

  override def get[T: ClassTag](key: String): Option[T] = {
    Log.debug(s"""Getting "$key" from cache...""")

    CacheApi.get[T](key)
  }

  override def set[T: ClassTag](key: String, value: T, timeout: FiniteDuration = timeout): Unit = {
    Log.debug(s"""Setting "$key" to cache...""")

    CacheApi.set(key, value, timeout)
  }

  override def remove(key: String): Unit = {
    Log.debug(s"""Removing "$key" from cache...""")

    CacheApi.remove(key)
  }
}
