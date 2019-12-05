ThisBuild / organization := "io.latis-data"
ThisBuild / scalaVersion := "2.12.8"

val http4sVersion = "0.20.13"

lazy val root = (project in file("."))
  .settings(compilerFlags)
  .settings(
    name := "hapi-server",
    libraryDependencies ++= Seq(
      "io.latis-data" %% "latis3-core" % "0.1.0-SNAPSHOT",
      "io.latis-data" %% "latis3-service-interface" % "0.1.0-SNAPSHOT",
      "io.latis-data" %% "latis3-server" % "0.1.0-SNAPSHOT",
      "io.latis-data" %% "latis3-hapi" % "0.1.0-SNAPSHOT",
      "io.latis-data" %% "dap2-service-interface" % "0.1.0-SNAPSHOT",
      "org.http4s" %% "http4s-dsl" % http4sVersion % Provided,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.http4s" %% "http4s-scalatags" % http4sVersion,
      "io.circe" %% "circe-generic" % "0.12.3",
      // coursier only seems to include compile dependencies when
      // building a standalone executable (see coursier/coursier#552)
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    )
  )

lazy val compilerFlags = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "utf-8",
    "-feature",
    "-language:higherKinds",
    "-unchecked",
    "-Xfuture",
    "-Xlint:-unused,_",
    "-Ypartial-unification",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused",
    "-Ywarn-value-discard"
  ),
  Compile / console / scalacOptions --= Seq(
    "-Ywarn-unused"
  )
)
