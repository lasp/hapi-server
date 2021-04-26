ThisBuild / organization := "io.latis-data"
ThisBuild / scalaVersion := "2.13.3"

val nexus = "https://artifacts.pdmz.lasp.colorado.edu/repository/"

val http4sVersion = "0.20.13"
val latisVersion = "190a220b0abe02ace93940d81c8606bd35ccae62" //newest: "322efe5eb661d72b1ed688ca3c9f91fadb9fd4ef"
val latis3HapiVersion = "59f1d2391f3670c20773d0908c0c6ba3f73f0f8d"

lazy val `latis3-core` = ProjectRef(
  uri(s"git://github.com/latis-data/latis3.git#$latisVersion"),
  "core"
)

lazy val `latis3-service-interface` = ProjectRef(
  uri(s"git://github.com/latis-data/latis3.git#$latisVersion"),
  "service-interface"
)

lazy val `latis3-server` = ProjectRef(
  uri(s"git://github.com/latis-data/latis3.git#$latisVersion"),
  "server"
)

lazy val `latis3-hapi` = ProjectRef(
  uri(s"git://github.com/latis-data/latis3-hapi.git#$latis3HapiVersion"),
  "server"
)

lazy val root = (project in file("."))
  .dependsOn(`latis3-core`)
  .dependsOn(`latis3-service-interface`)
  .dependsOn(`latis3-server`)
  .dependsOn(`latis3-hapi`)
  .settings(compilerFlags)
  .settings(
    name := "hapi-server",
    libraryDependencies ++= Seq(
      "io.latis-data" %% "dap2-service-interface" % "0.1.0-SNAPSHOT",
      "org.http4s" %% "http4s-dsl" % http4sVersion % Provided,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.http4s" %% "http4s-scalatags" % http4sVersion,
      "io.circe" %% "circe-generic" % "0.12.3",
      // coursier only seems to include compile dependencies when
      // building a standalone executable (see coursier/coursier#552)
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    ),
    resolvers ++= Seq(
      "Nexus Release" at nexus + "web-releases",
      "Nexus Snapshot" at nexus + "web-snapshots"
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
