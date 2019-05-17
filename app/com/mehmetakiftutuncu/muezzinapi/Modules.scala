package com.mehmetakiftutuncu.muezzinapi

import com.google.inject.AbstractModule
import com.mehmetakiftutuncu.muezzinapi.heartbeat.Heartbeat

class Modules extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Heartbeat]).asEagerSingleton()
  }
}
