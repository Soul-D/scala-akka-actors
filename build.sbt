lazy val root = (project in file("."))
  .settings(
    organization := "at.co.sdt",
    name := "scala-akka-actors",
    version := "1.4.0-SNAPSHOT",
    scalaVersion := "2.12.4",
    libraryDependencies ++= Dependencies.deps,
    // parallelExecution in Test := false
  )

scalacOptions += "-deprecation"
