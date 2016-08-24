package com.mehmetakiftutuncu.muezzinapi.utilities

import org.specs2.mutable.Specification

class HtmlSanitizerSpec extends Specification {
  "Sanitizing HTML" should {
    "uppercase each word properly" in {
      val html: String = "foo Bar baz"
      val expectedResult: String = "Foo Bar Baz"

      val result: String = HtmlSanitizer.sanitizeHtml(html, replaceHtmlChars = false, upperCaseEachWord = true)

      result mustEqual expectedResult
    }

    "replace HTML characters properly" in {
      val html: String = "mehmet akif t&#252;t&#252;nc&#252;"
      val expectedResult: String = "mehmet akif tütüncü"

      val result: String = HtmlSanitizer.sanitizeHtml(html, replaceHtmlChars = true, upperCaseEachWord = false)

      result mustEqual expectedResult
    }

    "replace HTML characters and uppercase each word properly" in {
      val html: String = "mehmet akif t&#252;t&#252;nc&#252;"
      val expectedResult: String = "Mehmet Akif Tütüncü"

      val result: String = HtmlSanitizer.sanitizeHtml(html, replaceHtmlChars = true, upperCaseEachWord = true)

      result mustEqual expectedResult
    }
  }
}
