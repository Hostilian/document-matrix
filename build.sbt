ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "3.4.2"
ThisBuild / version := "0.1.0"

lazy val root = (project in file("."))
  .settings(
    name := "document-matrix",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.21",
      "dev.zio" %% "zio-test" % "2.0.21" % Test,
      "dev.zio" %% "zio-test-sbt" % "2.0.21" % Test,
      "dev.zio" %% "zio-test-magnolia" % "2.0.21" % Test,
      "dev.zio" %% "zio-json" % "0.6.2",
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.typelevel" %% "cats-effect" % "3.5.4",
      "dev.zio" %% "zio-interop-cats" % "23.1.0.0",
      "dev.zio" %% "zio-http" % "3.0.0-RC2",
      "com.lihaoyi" %% "fansi" % "0.5.0",
      "org.jline" % "jline-terminal" % "3.24.1",
      "org.jline" % "jline-reader" % "3.24.1"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Wunused:all",
      "-Wvalue-discard"
    )
  )

// Assembly settings
assembly / assemblyMergeStrategy := {
  case "META-INF/services/*" => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case "reference.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}

assembly / assemblyJarName := "document-matrix.jar"
assembly / mainClass := Some("com.example.DocumentHttpApi")
