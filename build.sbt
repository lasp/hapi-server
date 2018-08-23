ThisBuild / organization := "lasp"
ThisBuild / scalaVersion := "2.12.6"

val http4sVersion = "0.18.15"

lazy val `hapi-server` = (project in file("."))
  .enablePlugins(DockerPlugin)
  .settings(compilerFlags)
  .settings(dockerSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s"    %% "http4s-blaze-server" % http4sVersion,
      "org.http4s"    %% "http4s-circe"        % http4sVersion,
      "org.http4s"    %% "http4s-dsl"          % http4sVersion,
      "ch.qos.logback" % "logback-classic"     % "1.2.3" % Runtime,
      "com.lihaoyi"   %% "scalatags"           % "0.6.7",
      "org.scalatest" %% "scalatest"           % "3.0.5" % Test
    ),
    Test / logBuffered := false
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
    val classpath = (Runtime / managedClasspath).value
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
      entryPoint("java", "-cp", cp, mainclass)
    }
  }
)
