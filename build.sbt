// === Project Definition ===

description          in ThisBuild := "A web server application for Islamic prayer times"
homepage             in ThisBuild := Some(url("https://github.com/makiftutuncu/MuezzinAPI"))
startYear            in ThisBuild := Some(2020)
licenses             in ThisBuild := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))
organization         in ThisBuild := "dev.akif"
organizationName     in ThisBuild := "Mehmet Akif Tütüncü"
organizationHomepage in ThisBuild := Some(url("https://akif.dev"))
developers           in ThisBuild := List(Developer("makiftutuncu", "Mehmet Akif Tütüncü", "m.akif.tutuncu@gmail.com", url("https://akif.dev")))
scmInfo              in ThisBuild := Some(ScmInfo(url("https://github.com/makiftutuncu/MuezzinAPI"), "git@github.com:makiftutuncu/MuezzinAPI.git"))
version              in ThisBuild := "3.0.0-SNAPSHOT"

// === Modules ===

lazy val `muezzin-api` = project
  .in(file("."))
  .settings(Settings.scalaSettings)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.akkaHttp,
      Dependencies.akkaStream,
      Dependencies.circeCore,
      Dependencies.circeParser,
      Dependencies.durum,
      Dependencies.eScala,
      Dependencies.eCirce,
      Dependencies.eZio,
      Dependencies.zio,
      Dependencies.scalaTest
    )
  )
