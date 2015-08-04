name := """MuezzinAPI"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  ws,
  cache,
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41"
)
