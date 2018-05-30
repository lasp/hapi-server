ThisBuild / organization := "lasp"
ThisBuild / scalaVersion := "2.12.6"

lazy val `latis-hapi` = (project in file("."))
  .settings(compilerFlags)
  .settings(
    name := "latis-hapi"
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
