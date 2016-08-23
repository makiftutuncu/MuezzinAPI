package com.mehmetakiftutuncu.muezzinapi.controllers

import javax.inject.Singleton

import com.mehmetakiftutuncu.muezzinapi.utilities.ControllerBase
import play.api.mvc.{Action, AnyContent}

@Singleton
class ApplicationController extends ControllerBase {
  def index: Action[AnyContent] = Action(Ok("Welcome to Muezzin API!"))

  def empty: Action[AnyContent] = Action(Ok)
}
