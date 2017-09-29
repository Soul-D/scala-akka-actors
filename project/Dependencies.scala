import sbt._

object Dependencies {
  val akkaVersion = "2.5.4"
  val slf4jVersion = "1.7.25"

  val akkaOrg = "com.typesafe.akka"
  val slf4jOrg = "org.slf4j"

  val deps = Seq(
    akkaOrg %% "akka-actor" % akkaVersion,
    akkaOrg %% "akka-testkit" % akkaVersion,
    slf4jOrg % "slf4j-simple" % slf4jVersion,
    // "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  )
}
