package com.mehmetakiftutuncu.muezzinapi.controllers

import com.github.mehmetakiftutuncu.errors.{CommonError, Errors}
import com.mehmetakiftutuncu.muezzinapi.models._
import com.mehmetakiftutuncu.muezzinapi.services._
import com.mehmetakiftutuncu.muezzinapi.utilities.ControllerExtras
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class PrayerTimesController @Inject()(ControllerComponents: ControllerComponents,
                                      CountryService: AbstractCountryService,
                                      CityService: AbstractCityService,
                                      DistrictService: AbstractDistrictService,
                                      PrayerTimesService: AbstractPrayerTimesService) extends AbstractController(ControllerComponents) with ControllerExtras {
  def getPrayerTimes(countryId: Int, cityId: Int, districtId: Option[Int]): Action[AnyContent] = Action.async {
    val log: String = s"""Failed to get prayer times for country "$countryId", city "$cityId" and district "$districtId"!"""

    CountryService.getCountries.flatMap {
      case Left(countryErrors: Errors) =>
        futureFailWithErrors(log, countryErrors)

      case Right(countries: List[Country]) =>
        val countryAsOpt: Option[Country] = countries.find(_.id == countryId)

        if (countryAsOpt.isEmpty) {
          futureFailWithErrors(log, Errors(CommonError.notFound.data(countryId.toString)))
        } else {
          CityService.getCities(countryId).flatMap {
            case Left(cityErrors: Errors) =>
              futureFailWithErrors(log, cityErrors)

            case Right(cities: List[City]) =>
              val cityAsOpt: Option[City] = cities.find(_.id == cityId)

              if (cityAsOpt.isEmpty) {
                futureFailWithErrors(log, Errors(CommonError.notFound.data(cityId.toString)))
              } else {
                DistrictService.getDistricts(countryId, cityId).flatMap {
                  case Left(districtErrors: Errors) =>
                    futureFailWithErrors(log, districtErrors)

                  case Right(districts: List[District]) =>
                    val districtAsOpt: Option[District] = districtId.flatMap(did => districts.find(_.id == did))

                    if (districtId.isDefined && districtAsOpt.isEmpty) {
                      futureFailWithErrors(log, Errors(CommonError.invalidRequest.reason(s"""City "$cityId" doesn't have district "${districtId.get}"!""")))
                    } else if (districtId.isEmpty && districts.nonEmpty) {
                      futureFailWithErrors(log, Errors(CommonError.invalidRequest.reason(s"""District id is not provided but city "$cityId" has districts available!""")))
                    } else {
                      val place: Place = Place(countryId, Some(cityId), districtId)

                      PrayerTimesService.getPrayerTimes(place).map {
                        case Left(prayerTimesErrors: Errors) =>
                          failWithErrors(log, prayerTimesErrors)

                        case Right(prayerTimes: List[PrayerTimesOfDay]) =>
                          val result: JsObject = Json.obj(
                            "prayerTimes" -> prayerTimes.foldLeft(Json.obj())(_ ++ _.toJson)
                          )

                          success(result)
                      }
                    }
                }
              }
          }
        }
    }
  }
}
