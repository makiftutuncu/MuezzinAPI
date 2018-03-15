package com.mehmetakiftutuncu.muezzinapi.controllers

import com.mehmetakiftutuncu.muezzinapi.utilities.ControllerExtras
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

@Singleton
class ApplicationController @Inject()(controllerComponents: ControllerComponents) extends AbstractController(controllerComponents) with ControllerExtras {
  def index: Action[AnyContent] = Action(Ok("Welcome to Muezzin API!"))

  def empty: Action[AnyContent] = Action(Ok)
}
