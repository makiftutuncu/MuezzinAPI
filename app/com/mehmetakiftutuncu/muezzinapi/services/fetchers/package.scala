package com.mehmetakiftutuncu.muezzinapi.services

import org.jsoup.Jsoup

import scala.collection.JavaConverters._
import org.jsoup.nodes.{Document, Element}

package object fetchers {
  def parseListOf[T](html: String, cssId: String)(construct: (Int, String) => T): List[T] = {
    val document: Document = Jsoup.parse(html)
    val options: List[Element] = document.select(s"#$cssId").select("option").asScala.toList

    for {
      (rawId: String, rawName: String) <- options.map(o => o.attr("value") -> o.text()) if rawId.nonEmpty
    } yield {
      val id: Int = rawId.toInt

      construct(id, rawName)
    }
  }
}
