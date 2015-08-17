package com.mehmetakiftutuncu.muezzinapi.controllers

import com.mehmetakiftutuncu.muezzinapi.models.base.MuezzinAPIController
import play.api.mvc._

/**
 * Main controller of the application, only for welcoming a user
 */
object Application extends MuezzinAPIController {
  def index = Action {
    Ok("Welcome to Muezzin API!")
  }
}
