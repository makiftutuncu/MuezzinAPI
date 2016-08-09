package com.mehmetakiftutuncu.muezzinapi.controllers

import javax.inject.Singleton

import play.api.mvc.{Action, AnyContent, Controller}

@Singleton
class ApplicationController extends Controller {
  def index: Action[AnyContent] = Action {
    Ok
  }
}
