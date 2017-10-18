name := """MuezzinAPI"""

version := "2.2"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  cache,
  ws,
  "com.google.firebase"           % "firebase-admin" % "5.0.0",
  "com.github.mehmetakiftutuncu" %% "errors"         % "1.1",
  "com.typesafe.akka"            %% "akka-actor"     % "2.4.17",
  "org.jsoup"                     % "jsoup"          % "1.10.3",
  "org.specs2"                   %% "specs2-core"    % "3.9.0" % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

routesImport += "com.mehmetakiftutuncu.muezzinapi.utilities.OptionIntPathBindable._"

scalacOptions in Test ++= Seq("-Yrangepos")
