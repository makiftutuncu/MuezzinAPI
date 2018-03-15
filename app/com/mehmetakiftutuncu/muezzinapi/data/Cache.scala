package com.mehmetakiftutuncu.muezzinapi.data

import java.util.concurrent.TimeUnit

import com.google.inject.ImplementedBy
import com.mehmetakiftutuncu.muezzinapi.utilities.{AbstractConf, Log, Logging}
import javax.inject.{Inject, Singleton}
import play.api.cache.AsyncCacheApi

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

@ImplementedBy(classOf[Cache])
trait AbstractCache {
  protected val timeout: FiniteDuration

  def get[T: ClassTag](key: String)(implicit ec: ExecutionContext): Future[Option[T]]
  def set[T: ClassTag](key: String, value: T, timeout: FiniteDuration = timeout)(implicit ec: ExecutionContext): Future[Unit]
  def remove(key: String)(implicit ec: ExecutionContext): Future[Unit]
  def flush()(implicit ec: ExecutionContext): Future[Unit]
}

@Singleton
class Cache @Inject()(CacheApi: AsyncCacheApi, Conf: AbstractConf) extends AbstractCache with Logging {
  override protected val timeout: FiniteDuration = Conf.getFiniteDuration("muezzinApi.cache.timeout", FiniteDuration(1, TimeUnit.DAYS))

  override def get[T: ClassTag](key: String)(implicit ec: ExecutionContext): Future[Option[T]] = {
    Log.debug(s"""Getting "$key" from cache...""")

    CacheApi.get[T](key)
  }

  override def set[T: ClassTag](key: String, value: T, timeout: FiniteDuration = timeout)(implicit ec: ExecutionContext): Future[Unit] = {
    Log.debug(s"""Setting "$key" to cache...""")

    CacheApi.set(key, value, timeout).map(_ => ())
  }

  override def remove(key: String)(implicit ec: ExecutionContext): Future[Unit] = {
    Log.debug(s"""Removing "$key" from cache...""")

    CacheApi.remove(key).map(_ => ())
  }

  override def flush()(implicit ec: ExecutionContext): Future[Unit] = {
    Log.debug(s"""Flushing cache...""")

    CacheApi.removeAll().map(_ => ())
  }
}
