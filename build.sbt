name := """MuezzinAPI"""

version := "2.4.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala, ElasticBeanstalkPlugin)

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  ehcache,
  guice,
  ws,
  "com.google.firebase"           % "firebase-admin" % "5.9.0",
  "com.github.mehmetakiftutuncu" %% "errors"         % "1.2",
  "com.typesafe.akka"            %% "akka-actor"     % "2.5.11",
  "org.jsoup"                     % "jsoup"          % "1.11.2",
  "org.specs2"                   %% "specs2-core"    % "4.0.2" % Test,
  "org.specs2"                   %% "specs2-junit"   % "4.0.2" % Test
)

resolvers += Resolver.sbtPluginRepo("releases")

routesImport += "com.mehmetakiftutuncu.muezzinapi.utilities.OptionIntPathBindable._"

scalacOptions in Test ++= Seq("-Yrangepos")

packageName in ElasticBeanstalk := "muezzin-api"
elasticBeanstalkSources in ElasticBeanstalk := Seq(
  baseDirectory.value / "Dockerrun.aws.json"
)
dockerLabels := Map(
 "maintainer" -> "Mehmet Akif Tütüncü <m.akif.tutuncu@gmail.com>"
)
dockerExposedPorts := Seq(9000)
dockerBaseImage := "java:latest"
