import sbt._

object Dependencies {
  lazy val akkaHttp    = "com.typesafe.akka" %% "akka-http"    % "10.1.11"
  lazy val akkaStream  = "com.typesafe.akka" %% "akka-stream"  % "2.5.26"
  lazy val circeCore   = "io.circe"          %% "circe-core"   % "0.13.0"
  lazy val circeParser = "io.circe"          %% "circe-parser" % "0.13.0"
  lazy val durum       = "dev.akif"          %% "durum-core"   % "0.1.0-SNAPSHOT"
  lazy val eScala      = "dev.akif"          %% "e-scala"      % "1.1.3-SNAPSHOT"
  lazy val eCirce      = "dev.akif"          %% "e-circe"      % "1.1.3-SNAPSHOT"
  lazy val eZio        = "dev.akif"          %% "e-zio"        % "1.1.3-SNAPSHOT"
  lazy val zio         = "dev.zio"           %% "zio"          % "1.0.0-RC17"
  lazy val scalaTest   = "org.scalatest"     %% "scalatest"    % "3.1.0" % Test
}
