ThisBuild / organization := "lasp"
ThisBuild / scalaVersion := "2.12.6"

val http4sVersion = "0.18.15"

lazy val `latis-hapi` = (project in file("."))
  .settings(compilerFlags)
  .settings(
    name := "latis-hapi",
    libraryDependencies ++= Seq(
      "org.http4s"    %% "http4s-blaze-server" % http4sVersion,
      "org.http4s"    %% "http4s-dsl"          % http4sVersion,
      "ch.qos.logback" % "logback-classic"     % "1.2.3" % Runtime
    )
  )

lazy val compilerFlags = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "utf-8",
    "-feature",
    "-language:higherKinds",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xfuture",
    "-Xlint",
    "-Ypartial-unification",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused",
    "-Ywarn-value-discard"
  ),
  Compile / console / scalacOptions --= Seq(
    "-Xfatal-warnings",
    "-Ywarn-unused"
  )
)
