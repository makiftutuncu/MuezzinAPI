name := """MuezzinAPI"""

version := "2.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  cache,
  ws,
  "com.google.firebase"           % "firebase-server-sdk" % "[3.0.0,)",
  "com.github.mehmetakiftutuncu" %% "errors"              % "1.1",
  "com.typesafe.akka"            %% "akka-actor"          % "2.4.9",
  "org.specs2"                   %% "specs2-core"         % "3.8.4" % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

routesImport += "com.mehmetakiftutuncu.muezzinapi.utilities.OptionIntPathBindable._"

scalacOptions in Test ++= Seq("-Yrangepos")
