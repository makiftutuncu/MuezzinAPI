package dev.akif.muezzinapi.translation

trait Translator[A] {
  def nameIn(a: A, language: Language): Option[String]
}

object Translator {
  def apply[A](implicit translator: Translator[A]): Translator[A] = translator

  def nameIn[A](a: A, language: Language)(implicit translator: Translator[A]): Option[String] = translator.nameIn(a, language)
}
