package com.mehmetakiftutuncu.muezzinapi.utilities

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import play.api.libs.ws.{WSClient, WSRequest}

import scala.concurrent.duration.FiniteDuration

@ImplementedBy(classOf[WS])
trait AbstractWS {
  protected val timeout: FiniteDuration

  def url(url: String): WSRequest
}

@Singleton
class WS @Inject()(Conf: AbstractConf, WSClient: WSClient) extends AbstractWS with Logging {
  override protected val timeout: FiniteDuration = Conf.getFiniteDuration("muezzinApi.ws.timeout", FiniteDuration(15, TimeUnit.SECONDS))

  override def url(url: String): WSRequest = {
    Log.debug(s"""Building a WS request to url "$url"...""")

    WSClient.url(url).withRequestTimeout(timeout)
  }
}
