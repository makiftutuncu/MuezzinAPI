package com.mehmetakiftutuncu.muezzinapi.extractors.prayertimes

import com.mehmetakiftutuncu.muezzinapi.models.prayertimes.District
import com.mehmetakiftutuncu.muezzinapi.utilities.error.{Errors, SingleError}
import com.mehmetakiftutuncu.muezzinapi.utilities.{Conf, Log, Utils, Web}
import play.api.libs.json.{JsArray, JsValue}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

object DistrictExtractor {
   def extractCities(city: Int): Future[Either[Errors, List[District]]] = {
     Web.getForJson(Conf.Url.districts.format(city)) map {
       case Left(getPageErrors) =>
         Left(getPageErrors)

       case Right(districtsPage) =>
         parseDistricts(districtsPage)
     }
   }

   private def parseDistricts(page: JsValue): Either[Errors, List[District]] = {
     val districtsJsonAsOpt = page.asOpt[JsArray]

     if (districtsJsonAsOpt.isEmpty) {
       Log.error(s"""Failed to parse districts. Page has invalid format: $page""", "DistrictExtractor")
       Left(Errors(SingleError.InvalidData.withDetails("Districts page have invalid format.")))
     } else {
       val districtsJs = districtsJsonAsOpt.get.value.toList

       if (districtsJs.exists(j => (j \ "Text").asOpt[String].isEmpty || (j \ "Value").asOpt[String].flatMap(s => Try(s.toInt).toOption).isEmpty)) {
         Log.error(s"""Failed to parse districts. Found some invalid districts in page: $page""", "DistrictExtractor")
         Left(Errors(SingleError.InvalidData.withDetails("Invalid districts are found in page.")))
       } else {
         val districts = districtsJs map {
           districtJs =>
             val id   = (districtJs \ "Value").as[String].toInt
             val name = Utils.sanitizeHtml((districtJs \ "Text").as[String])

             District(id, name)
         }

         val sortedDistricts = districts.sortBy(_.name)

         Right(sortedDistricts)
       }
     }
   }
 }
