package com.mehmetakiftutuncu.muezzinapi

import com.google.inject.AbstractModule
import com.mehmetakiftutuncu.muezzinapi.broom.Broom
import com.mehmetakiftutuncu.muezzinapi.heartbeat.Heartbeat
import com.mehmetakiftutuncu.muezzinapi.shovel.Shovel

class Modules extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Broom]).asEagerSingleton()
    bind(classOf[Heartbeat]).asEagerSingleton()
    bind(classOf[Shovel]).asEagerSingleton()
  }
}
