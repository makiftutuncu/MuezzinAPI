package com.mehmetakiftutuncu.muezzinapi.utilities

import java.util.Locale

object HtmlSanitizer {
  def sanitizeHtml(str: String,
                   replaceHtmlChars: Boolean = true,
                   upperCaseEachWord: Boolean = true,
                   locale: Locale = Locale.getDefault): String = {
    val uppercasedResult: String = if (upperCaseEachWord) {
      str.toLowerCase(locale)
        .split(" ")
        .map(w => w.take(1).toUpperCase(locale) + w.drop(1))
        .mkString(" ")
    } else {
      str
    }

    val replacedResult: String = if (replaceHtmlChars) {
      uppercasedResult.replaceAll("&#304;", "İ")
                      .replaceAll("&#214;", "Ö")
                      .replaceAll("&#220;", "Ü")
                      .replaceAll("&#199;", "Ç")
                      .replaceAll("&#286;", "Ğ")
                      .replaceAll("&#350;", "Ş")
                      .replaceAll("&#305;", "ı")
                      .replaceAll("&#246;", "ö")
                      .replaceAll("&#252;", "ü")
                      .replaceAll("&#231;", "ç")
                      .replaceAll("&#287;", "ğ")
                      .replaceAll("&#351;", "ş")
    } else {
      uppercasedResult
    }

    replacedResult.replaceAll("[^\\pL\\s]", "").trim
  }
}
