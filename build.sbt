ThisBuild / scalaVersion := "2.13.2"
ThisBuild / organization := "com.innerproduct"
ThisBuild / version := "0.0.1-SNAPSHOT"
ThisBuild / fork := true

val CatsEffectVersion = "2.1.3"
val CatsTaglessVersion = "0.11"
val CirceVersion = "0.13.0"
val Http4sVersion = "0.21.4"
val LogbackVersion = "1.2.3"
val MunitVersion = "0.7.8"

val commonSettings =
  Seq(
    addCompilerPlugin(
      "org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full
    ),
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % MunitVersion % Test
    ),
    // scalacOptions provided by sbt-tpolecat plugin
    testFrameworks += new TestFramework("munit.Framework")
  )

lazy val exercises = (project in file("exercises"))
  .settings(commonSettings)
  .settings(
    scalacOptions += "-Wunused:nowarn",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % CatsEffectVersion,
      "org.typelevel" %% "cats-effect-laws" % CatsEffectVersion % Test
    )
  )

lazy val petstore = (project in file("case-studies") / "petstore")
  .dependsOn(exercises % "test->test;compile->compile")
  .settings(commonSettings)
  .settings(
    // -Ymacro-annotations in 2.13.2 breaks -Wunused-imports, so downgrade for petstore (https://github.com/scala/bug/issues/11978)
    scalaVersion := "2.13.1",
    scalacOptions += "-Ymacro-annotations", // required by cats-tagless-macros
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.typelevel" %% "cats-tagless-macros" % CatsTaglessVersion,
      "org.scalameta" %% "munit-scalacheck" % MunitVersion % Test
    )
  )
