ThisBuild / organization := "io.latis-data"
ThisBuild / scalaVersion := "2.13.5"

val nexus = "https://artifacts.pdmz.lasp.colorado.edu/repository/"

val http4sVersion = "0.21.22"
val latisVersion = "322efe5e"

//lazy val `latis3-hapi` = ProjectRef(file("../latis3-hapi"), "hapi")

lazy val root = (project in file("."))
  .settings(compilerFlags)
  //.dependsOn(`latis3-hapi`)
  .settings(
    name := "hapi-server",
    libraryDependencies ++= Seq(
      "com.github.latis-data.latis3" %% "latis3-core"              % latisVersion,
      "com.github.latis-data.latis3" %% "latis3-service-interface" % latisVersion,
      "com.github.latis-data.latis3" %% "latis3-server"            % latisVersion,
      "com.github.latis-data.latis3" %% "dap2-service-interface"   % latisVersion,
      //"com.github.latis-data"        %% "latis3-hapi"              % "59f1d239",
      "org.http4s"                   %% "http4s-dsl"               % http4sVersion % Provided,
      "org.http4s"                   %% "http4s-circe"             % http4sVersion,
      "org.http4s"                   %% "http4s-scalatags"         % http4sVersion,
      "io.circe"                     %% "circe-generic"            % "0.13.0",
      // coursier only seems to include compile dependencies when
      // building a standalone executable (see coursier/coursier#552)
      "ch.qos.logback"                % "logback-classic"          % "1.2.3"
    ),
    resolvers ++= Seq(
      "Nexus Release" at nexus + "web-releases",
      "Nexus Snapshot" at nexus + "web-snapshots",
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
    "-Xfuture",
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
