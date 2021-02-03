ThisBuild / organization := "lasp"
ThisBuild / scalaVersion := "2.12.13"

val http4sVersion     = "0.21.7"
val pureconfigVersion = "0.13.0"

lazy val `hapi-server` = (project in file("."))
  .enablePlugins(DockerPlugin)
  .dependsOn(latis2)
  .settings(compilerFlags)
  .settings(dockerSettings)
  .settings(assemblySettings)
  .settings(
    libraryDependencies ++= Seq(
      "io.circe"              %% "circe-generic"          % "0.13.0",
      "org.http4s"            %% "http4s-blaze-server"    % http4sVersion,
      "org.http4s"            %% "http4s-circe"           % http4sVersion,
      "org.http4s"            %% "http4s-dsl"             % http4sVersion,
      "org.http4s"            %% "http4s-scalatags"       % http4sVersion,
      "org.log4s"             %% "log4s"                  % "1.6.1",
      "ch.qos.logback"         % "logback-classic"        % "1.2.3" % Runtime,
      "com.github.pureconfig" %% "pureconfig"             % pureconfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % pureconfigVersion,
      "org.scalatest"         %% "scalatest"              % "3.0.5" % Test
    ),
    Test / logBuffered := false
  )

lazy val latis2 = ProjectRef(
  uri("git://github.com/latis-data/latis.git#78e60aeb5387a047ceb99db56a1bd49786100904"),
  "latis"
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
    "-Xlint:-unused,_",
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

lazy val dockerSettings = Seq(
  docker / imageNames := {
    Seq(ImageName(s"${organization.value}/${name.value}:${version.value}"))
  },
  docker / dockerfile := {
    val jarFile = (Compile / packageBin / sbt.Keys.`package`).value
    val classpath = Seq(
      (Runtime / managedClasspath).value,
      (Runtime / internalDependencyAsJars).value
    ).flatten
    val mainclass = (Compile / packageBin / mainClass).value.getOrElse {
      sys.error("Expected exactly one main class")
    }
    val jarTarget = s"/app/${jarFile.getName}"
    val cp = s"$jarTarget:" + classpath.files.map { x =>
      s"/app/${x.getName}"
    }.mkString(":")

    new Dockerfile {
      from("openjdk:8-jre-alpine")
      copy(classpath.files, "/app/")
      copy(jarFile, jarTarget)
      expose(8080)
      env("HAPI_CATALOG", "/srv/hapi")
      entryPoint("java", "-cp", cp, mainclass)
    }
  }
)

lazy val assemblySettings = Seq(
  assembly / assemblyMergeStrategy := {
    case "latis.properties" => MergeStrategy.first
    case x =>
      val strategy = (assemblyMergeStrategy in assembly).value
      strategy(x)
  }
)
