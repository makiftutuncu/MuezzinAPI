package com.mehmetakiftutuncu.muezzinapi.utilities

import com.mehmetakiftutuncu.muezzinapi.utilities.error.{Errors, SingleError}
import play.api.Play.current
import play.api.http.{ContentTypeOf, MimeTypes, Writeable}
import play.api.libs.json.JsValue
import play.api.libs.ws.{WS, WSResponse}
import play.mvc.Http

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * A utility object to send GET and POST requests to web and retrieve results in various types
 */
object Web {
  /**
   * Sends a GET request to given url and converts result to string
   *
   * @param url Url to sent GET request to
   *
   * @return Result as string or some errors
   */
  def getForHtml(url: String): Future[Either[Errors, String]] = {
    Log.debug(s"Sending GET request for HTML to url: $url", "Web")

    get[String](url) {
      response: WSResponse =>
        val status: Int = response.status
        val contentType: String = response.header("Content-Type").getOrElse("")

        if (status != Http.Status.OK) {
          Log.error(s"GET request for HTML failed, received invalid status! url: $url, status: $status", "Web")
          Left(Errors(SingleError.RequestFailed.withValue("status").withDetails(status.toString)))
        } else if (!contentType.contains(MimeTypes.HTML)) {
          Log.error(s"GET request for HTML failed, received invalid content type! url: $url, content type: $contentType", "Web")
          Left(Errors(SingleError.RequestFailed.withValue("contentType").withDetails(contentType)))
        } else {
          Right(response.body)
        }
    }
  }

  /**
   * Sends a GET request to given url and converts result to [[play.api.libs.json.JsValue]]
   *
   * @param url Url to sent GET request to
   *
   * @return Result as Json or some errors
   */
  def getForJson(url: String): Future[Either[Errors, JsValue]] = {
    Log.debug(s"Sending GET request for Json to url: $url", "Web")

    get[JsValue](url) {
      response: WSResponse =>
        val status: Int = response.status
        val contentType: String = response.header("Content-Type").getOrElse("")

        if (status != Http.Status.OK) {
          Log.error(s"GET request for Json failed, received invalid status! url: $url, status: $status", "Web")
          Left(Errors(SingleError.RequestFailed.withValue("status").withDetails(status.toString)))
        } else if (!contentType.contains(MimeTypes.JSON)) {
          Log.error(s"GET request for Json failed, received invalid content type! url: $url, content type: $contentType", "Web")
          Left(Errors(SingleError.RequestFailed.withValue("contentType").withDetails(contentType)))
        } else {
          Right(response.json)
        }
    }
  }

  /**
   * Sends a POST request with a form body to given url and converts result to string
   *
   * @param url  Url to sent POST request to
   * @param form Data to post
   *
   * @return Result as string or some errors
   */
  def postForHtml(url: String, form: Map[String, Seq[String]] = Map.empty[String, Seq[String]]): Future[Either[Errors, String]] = {
    Log.debug(s"Sending POST request for HTML with $form as body to url: $url", "Web")

    post[Map[String, Seq[String]], String](url, form) {
      response: WSResponse =>
        val status: Int = response.status
        val contentType: String = response.header("Content-Type").getOrElse("")

        if (status != Http.Status.OK) {
          Log.error(s"POST request for HTML with $form as body failed, received invalid status! url: $url, status: $status", "Web")
          Left(Errors(SingleError.RequestFailed.withValue("status").withDetails(status.toString)))
        } else if (!contentType.contains(MimeTypes.HTML)) {
          Log.error(s"POST request for HTML with $form as body failed, received invalid content type! url: $url, content type: $contentType", "Web")
          Left(Errors(SingleError.RequestFailed.withValue("contentType").withDetails(contentType)))
        } else {
          Right(response.body)
        }
    }
  }

  /**
   * Sends a POST request with a Json body to given url and converts result to string
   *
   * @param url  Url to sent POST request to
   * @param json Json to post
   *
   * @return Result as string or some errors
   */
  def postForHtml(url: String, json: JsValue): Future[Either[Errors, String]] = {
    Log.debug(s"Sending POST request for HTML with $json as body to url: $url", "Web")

    post[JsValue, String](url, json) {
      response: WSResponse =>
        val status: Int = response.status
        val contentType: String = response.header("Content-Type").getOrElse("")

        if (status != Http.Status.OK) {
          Log.error(s"POST request for HTML with $json as body failed, received invalid status! url: $url, status: $status", "Web")
          Left(Errors(SingleError.RequestFailed.withValue("status").withDetails(status.toString)))
        } else if (!contentType.contains(MimeTypes.HTML)) {
          Log.error(s"POST request for HTML with $json as body failed, received invalid content type! url: $url, content type: $contentType", "Web")
          Left(Errors(SingleError.RequestFailed.withValue("contentType").withDetails(contentType)))
        } else {
          Right(response.body)
        }
    }
  }

  /**
   * Sends a POST request with a form body to given url and converts result to [[play.api.libs.json.JsValue]]
   *
   * @param url  Url to sent POST request to
   * @param form Data to post
   *
   * @return Result as Json or some errors
   */
  def postForJson(url: String, form: Map[String, Seq[String]] = Map.empty[String, Seq[String]]): Future[Either[Errors, JsValue]] = {
    Log.debug(s"Sending POST request for HTML with $form as body to url: $url", "Web")

    post[Map[String, Seq[String]], JsValue](url, form) {
      response: WSResponse =>
        val status: Int = response.status
        val contentType: String = response.header("Content-Type").getOrElse("")

        if (status != Http.Status.OK) {
          Log.error(s"POST request for HTML with $form as body failed, received invalid status! url: $url, status: $status", "Web")
          Left(Errors(SingleError.RequestFailed.withValue("status").withDetails(status.toString)))
        } else if (!contentType.contains(MimeTypes.HTML)) {
          Log.error(s"POST request for HTML with $form as body failed, received invalid content type! url: $url, content type: $contentType", "Web")
          Left(Errors(SingleError.RequestFailed.withValue("contentType").withDetails(contentType)))
        } else {
          Right(response.json)
        }
    }
  }

  /**
   * Sends a POST request with a Json body to given url and converts result to [[play.api.libs.json.JsValue]]
   *
   * @param url  Url to sent POST request to
   * @param json Json to post
   *
   * @return Result as Json or some errors
   */
  def postForJson(url: String, json: JsValue): Future[Either[Errors, JsValue]] = {
    Log.debug(s"Sending POST request for HTML with $json as body to url: $url", "Web")

    post[JsValue, JsValue](url, json) {
      response: WSResponse =>
        val status: Int = response.status
        val contentType: String = response.header("Content-Type").getOrElse("")

        if (status != Http.Status.OK) {
          Log.error(s"POST request for HTML with $json as body failed, received invalid status! url: $url, status: $status", "Web")
          Left(Errors(SingleError.RequestFailed.withValue("status").withDetails(status.toString)))
        } else if (!contentType.contains(MimeTypes.HTML)) {
          Log.error(s"POST request for HTML with $json as body failed, received invalid content type! url: $url, content type: $contentType", "Web")
          Left(Errors(SingleError.RequestFailed.withValue("contentType").withDetails(contentType)))
        } else {
          Right(response.json)
        }
    }
  }

  /**
   * Sends a GET request to given url and converts result to [[play.api.libs.json.JsValue]]
   *
   * @param url Url to sent GET request to
   *
   * @return Result as Json or some errors
   */
  def getJson(url: String): Future[Either[Errors, JsValue]] = {
    Log.debug(s"Sending GET request to get Json from url: $url", "Web")

    get[JsValue](url) {
      response: WSResponse =>
        val status: Int = response.status
        val contentType: String = response.header("Content-Type").getOrElse("")

        if (status != Http.Status.OK) {
          Log.error(s"Failed to get Json, received invalid status! url: $url, status: $status", "Web")
          Left(Errors(SingleError.RequestFailed.withValue("status").withDetails(status.toString)))
        } else if (!contentType.contains(MimeTypes.JSON)) {
          Log.error(s"Failed to get Json, received invalid content type! url: $url, content type: $contentType", "Web")
          Left(Errors(SingleError.RequestFailed.withValue("contentType").withDetails(contentType)))
        } else {
          Right(response.json)
        }
    }
  }

  /**
   * Sends a GET request to given url and processes result
   *
   * @param url   Url to sent GET request to
   * @param block A function to process result
   *
   * @tparam R Type of expected result
   *
   * @return A result of expected type or some errors
   */
  private def get[R](url: String)(block: (WSResponse => Either[Errors, R])): Future[Either[Errors, R]] = {
    WS.url(url).withRequestTimeout(Conf.wsTimeout).get() map {
      response: WSResponse =>
        block(response)
    }
  }

  /**
   * Sends a POST request to given url with given data and processes result
   *
   * @param url   Url to sent GET request to
   * @param body  Data to post
   * @param block A function to process result
   *
   * @tparam B Type of data to post
   * @tparam R Type of expected result
   *
   * @return A result of expected type or some errors
   */
  private def post[B, R](url: String, body: B)(block: (WSResponse => Either[Errors, R]))(implicit wrt: Writeable[B], ct: ContentTypeOf[B]): Future[Either[Errors, R]] = {
    WS.url(url).withRequestTimeout(Conf.wsTimeout).post(body) map {
      response: WSResponse =>
        block(response)
    }
  }
}
