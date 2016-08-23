package com.mehmetakiftutuncu.muezzinapi.controllers

import javax.inject.{Inject, Singleton}

import com.github.mehmetakiftutuncu.errors.{CommonError, Errors}
import com.mehmetakiftutuncu.muezzinapi.models.{City, Country, District}
import com.mehmetakiftutuncu.muezzinapi.services._
import com.mehmetakiftutuncu.muezzinapi.utilities.ControllerBase
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class PlaceController @Inject()(CountryService: AbstractCountryService,
                                CityService: AbstractCityService,
                                DistrictService: AbstractDistrictService) extends ControllerBase {
  def getCountries: Action[AnyContent] = Action.async {
    val log: String = s"""Failed to get countries!"""

    CountryService.getCountries.map {
      case Left(errors: Errors) =>
        failWithErrors(log, errors)

      case Right(countries: List[Country]) =>
        val result: JsObject = Json.obj(
          "countries" -> JsObject(countries.map(country => country.id.toString -> country.toJson))
        )

        success(result)
    }
  }

  def getCities(countryId: Int): Action[AnyContent] = Action.async {
    val log: String = s"""Failed to get cities for country "$countryId""""

    CityService.getCities(countryId).map {
      case Left(errors: Errors) =>
        failWithErrors(log, errors)

      case Right(cities: List[City]) =>
        val result: JsObject = Json.obj(
          "cities" -> JsObject(cities.map(city => city.id.toString -> city.toJson))
        )

        success(result)
    }
  }

  def getDistricts(countryId: Int, cityId: Int): Action[AnyContent] = Action.async {
    val log: String = s"""Failed to get districts for country "$countryId" and city "$cityId"!"""

    CityService.getCities(countryId).flatMap {
      case Left(cityErrors: Errors) =>
        futureFailWithErrors(log, cityErrors)

      case Right(cities: List[City]) =>
        if (!cities.exists(_.id == cityId)) {
          futureFailWithErrors(log, Errors(CommonError.invalidRequest.reason(s"""Country "$countryId" has no city "$cityId"!""")))
        } else {
          DistrictService.getDistricts(countryId, cityId).map {
            case Left(districtErrors: Errors) =>
              failWithErrors(log, districtErrors)

            case Right(districts: List[District]) =>
              val result: JsObject = Json.obj(
                "districts" -> JsObject(districts.map(district => district.id.toString -> JsString(district.name)))
              )

              success(result)
          }
        }
    }
  }
}
