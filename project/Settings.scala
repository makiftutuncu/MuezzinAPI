import sbt.Keys._
import sbt._

object Settings {
  lazy val latestScalaVersion = "2.13.1"

  lazy val scalaSettings: Seq[Setting[_]] = Seq(
    scalaVersion := latestScalaVersion,
    javacOptions ++= Seq("-source", "11"),

    libraryDependencies ++= Seq(
      Dependencies.scalaTest
    )
  )
}
