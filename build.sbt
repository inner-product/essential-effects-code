ThisBuild / scalaVersion := "2.13.12"
ThisBuild / organization := "com.innerproduct"
ThisBuild / version := "0.0.1-SNAPSHOT"
ThisBuild / fork := true

val CatsVersion = "2.9.0"
val CatsEffectVersion = "3.5.3"
val MUnitCatsEffectVersion = "1.0.7"

val commonSettings =
  Seq(
    addCompilerPlugin(
      "org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full
    ),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "munit-cats-effect-3" % MUnitCatsEffectVersion % Test
    ),
    testFrameworks += new TestFramework("munit.Framework")
  )

lazy val examples = (project in file("examples"))
  .dependsOn(exercises % "test->test;compile->compile")
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % CatsEffectVersion,
      "org.typelevel" %% "cats-effect-testkit" % CatsEffectVersion % Test
    ),
    // remove fatal warnings since examples have unused and dead code blocks
    scalacOptions --= Seq(
      "-Wdead-code",
      "-Xfatal-warnings"
    )
  )

lazy val exercises = (project in file("exercises"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % CatsEffectVersion,
      "org.typelevel" %% "cats-effect-testkit" % CatsEffectVersion % Test
    ),
    // remove fatal warnings since exercises have unused and dead code blocks
    scalacOptions --= Seq(
      "-Wdead-code",
      "-Wunused:params",
      "-Xfatal-warnings"
    )
  )
