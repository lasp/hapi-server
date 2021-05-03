ThisBuild / organization := "io.latis-data"
ThisBuild / scalaVersion := "2.13.5"

val http4sVersion = "0.21.22"
val latisVersion = "91b09198"

lazy val `latis3-hapi` = ProjectRef(file("../latis3-hapi"), "hapi") //TODO: pull from jitpack once PR gets merged

lazy val root = (project in file("."))
  .settings(compilerFlags)
  .dependsOn(`latis3-hapi`)
  .settings(
    name := "hapi-server",
    libraryDependencies ++= Seq(
      "com.github.latis-data.latis3" %% "latis3-core"              % latisVersion,
      "com.github.latis-data.latis3" %% "latis3-service-interface" % latisVersion,
      "com.github.latis-data.latis3" %% "latis3-server"            % latisVersion,
      "com.github.latis-data.latis3" %% "dap2-service-interface"   % latisVersion,
      //"com.github.latis-data"         % "latis3-hapi"              % "updates-SNAPSHOT", //TODO: update once PR gets merged
      "org.http4s"                   %% "http4s-dsl"               % http4sVersion % Provided,
      "org.http4s"                   %% "http4s-circe"             % http4sVersion,
      "org.http4s"                   %% "http4s-scalatags"         % http4sVersion,
      "io.circe"                     %% "circe-generic"            % "0.13.0",
      // coursier only seems to include compile dependencies when
      // building a standalone executable (see coursier/coursier#552)
      "ch.qos.logback"                % "logback-classic"          % "1.2.3"
    ),
    resolvers ++= Seq(
      "Unidata" at "https://artifacts.unidata.ucar.edu/content/repositories/unidata-releases",
      "jitpack" at "https://jitpack.io"
    )
  )

lazy val compilerFlags = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "utf-8",
    "-feature",
    "-language:higherKinds",
    "-unchecked",
    "-Xlint:-unused,_",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused",
    "-Ywarn-value-discard"
  ),
  Compile / console / scalacOptions --= Seq(
    "-Ywarn-unused"
  )
)
