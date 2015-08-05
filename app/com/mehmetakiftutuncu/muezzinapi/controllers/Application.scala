package com.mehmetakiftutuncu.muezzinapi.controllers

import com.mehmetakiftutuncu.muezzinapi.models.base.MuezzinAPIController
import play.api.mvc._

object Application extends MuezzinAPIController {
  def index = Action {
    Ok("Welcome to Muezzin API!")
  }
}
