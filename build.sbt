name := """MuezzinAPI"""

version := "1.2-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  ws,
  cache,
  evolutions,
  "com.typesafe.play" %% "anorm" % "2.4.0",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "com.typesafe.akka" %% "akka-actor" % "2.3.12",
  "joda-time" % "joda-time" % "2.8.2",
  "mysql" % "mysql-connector-java" % "5.1.27"
)
