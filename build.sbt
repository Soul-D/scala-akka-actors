name := "scala-akka-actors"

lazy val root = (project in file("."))
  .settings(
    organization := "at.co.sdt",
    version := "1.2.0",
    scalaVersion := "2.12.3",
    libraryDependencies ++= Dependencies.deps,
    // parallelExecution in Test := false
  )
