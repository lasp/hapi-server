ThisBuild / organization := "io.latis-data"
ThisBuild / scalaVersion := "2.13.10"

val fs2DataVersion = "1.6.0"
val http4sVersion = "0.23.16"
val latisVersion = "49a8367"
val latisHapiVersion = "55f5bb2"

lazy val root = (project in file("."))
  .settings(
    name := "hapi-server",
    libraryDependencies ++= Seq(
      "com.github.latis-data.latis3" %% "latis3-core"              % latisVersion,
      "com.github.latis-data.latis3" %% "latis3-service-interface" % latisVersion,
      "com.github.latis-data.latis3" %% "latis3-server"            % latisVersion,
      "com.github.latis-data.latis3" %% "dap2-service-interface"   % latisVersion,
      "com.github.latis-data"         % "latis3-hapi"              % latisHapiVersion,
      "org.http4s"                   %% "http4s-dsl"               % http4sVersion % Provided,
      "org.http4s"                   %% "http4s-circe"             % http4sVersion,
      "org.http4s"                   %% "http4s-scalatags"         % "0.24.0",
      "org.scalameta"                %% "munit"                    % "0.7.29" % Test,
      "org.typelevel"                %% "munit-cats-effect-3"      % "1.0.7" % Test,
      "io.circe"                     %% "circe-generic"            % "0.14.3",
      // coursier only seems to include compile dependencies when
      // building a standalone executable (see coursier/coursier#552)
      "ch.qos.logback"                % "logback-classic"          % "1.3.4" % Test,
      "org.gnieh"                    %% "fs2-data-json"            % fs2DataVersion,
      "org.gnieh"                    %% "fs2-data-json-circe"      % fs2DataVersion
    ),
    resolvers ++= Seq(
      "Unidata" at "https://artifacts.unidata.ucar.edu/content/repositories/unidata-releases",
      "jitpack" at "https://jitpack.io"
    ),
    reStart / mainClass := Some("latis.server.Latis3Server"),
    scalacOptions -= "-Xfatal-warnings",
    scalacOptions += {
      if (insideCI.value) "-Wconf:any:e" else "-Wconf:any:w"
    }
  )
