name := "document-matrix"
version := "0.1.0"
scalaVersion := "3.4.2"
libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "2.0.21",
  "dev.zio" %% "zio-test" % "2.0.21" % Test,
  "dev.zio" %% "zio-test-sbt" % "2.0.21" % Test,
  "org.typelevel" %% "cats-core" % "2.12.0",
  "dev.zio" %% "zio-interop-cats" % "23.1.0.0",
  "dev.zio" %% "zio-http" % "3.0.0-RC2",
  "com.lihaoyi" %% "fansi" % "0.4.0"
)
testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
ThisBuild / organization := "com.example"
