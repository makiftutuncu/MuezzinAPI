package com.mehmetakiftutuncu.muezzinapi.controllers

import java.time.Duration

import com.github.mehmetakiftutuncu.errors.{CommonError, Errors}
import com.google.firebase.database.DatabaseReference.CompletionListener
import com.google.firebase.database.{DatabaseError, DatabaseReference}
import com.mehmetakiftutuncu.muezzinapi.data.FirebaseRealtimeDatabase._
import com.mehmetakiftutuncu.muezzinapi.data.{AbstractCache, AbstractFirebaseRealtimeDatabase}
import com.mehmetakiftutuncu.muezzinapi.utilities.{AbstractConf, ControllerExtras, Log, Timer}
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise

@Singleton
class NukeController @Inject()(Cache: AbstractCache,
                               ControllerComponents: ControllerComponents,
                               Conf: AbstractConf,
                               FirebaseRealtimeDatabase: AbstractFirebaseRealtimeDatabase) extends AbstractController(ControllerComponents) with ControllerExtras {
  private val whatCanBeNuked: Set[String] = Set("countries", "prayerTimes")

  def nuke(target: String, code: String): Action[AnyContent] = Action.async {
    if (!whatCanBeNuked.contains(target)) {
      futureFailWithErrors("You don't know what you're nuking!", Errors(CommonError.invalidData.reason("Invalid nuke target!").data(target)))
    } else if (!Conf.getString("muezzinApi.nuke.code").contains(code)) {
      futureFailWithErrors("You don't know nuke codes!", Errors(CommonError.authorization.reason("Invalid nuke code!")))
    } else {
      Timer.start(s"nuke.$target")

      val reference: DatabaseReference = FirebaseRealtimeDatabase.root / target

      val promise: Promise[Errors] = Promise[Errors]()

      reference.removeValue(new CompletionListener {
        override def onComplete(databaseError: DatabaseError, databaseReference: DatabaseReference): Unit = {
          val errors: Errors = if (databaseError != null) {
            Errors(CommonError.database.reason(databaseError.toException.getMessage))
          } else {
            Errors.empty
          }

          promise.success(errors)
        }
      })

      promise.future.map { errors: Errors =>
        val duration: Duration = Timer.stop(s"nuke.$target")

        if (errors.hasErrors) {
          failWithErrors(s"""Failed to nuke "$target"!""", errors)
        } else {
          Log.debug(s"""Successfully nuked "$target" in ${duration.toMillis} ms.""")

          success(Json.obj("target" -> "destroyed"))
        }
      }
    }
  }

  def nukeCache(code: String): Action[AnyContent] = Action.async {
    if (!Conf.getString("muezzinApi.nuke.code").contains(code)) {
      futureFailWithErrors("You don't know nuke codes!", Errors(CommonError.authorization.reason("Invalid nuke code!")))
    } else {
      Timer.start(s"nuke.cache")

      Cache.flush().map { _ =>
        val duration: Duration = Timer.stop(s"nuke.cache")

        Log.debug(s"""Successfully nuked cache in ${duration.toMillis} ms.""")

        success(Json.obj("cache" -> "flushed"))
      }
    }
  }
}
