import sbt._

object Dependencies {
  val akkaVersion = "2.5.4"
  val slf4jVersion = "1.7.25"
  val scalaLoggingVersion = "3.7.2"
  val logbackVersion = "1.2.3"

  val akkaOrg = "com.typesafe.akka"
  val slf4jOrg = "org.slf4j"
  val scalaLoggingOrg = "com.typesafe.scala-logging"
  val logbackOrg = "ch.qos.logback"

  val deps = Seq(
    akkaOrg %% "akka-actor" % akkaVersion,
    akkaOrg %% "akka-testkit" % akkaVersion,
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    // slf4jOrg % "slf4j-simple" % slf4jVersion,
    scalaLoggingOrg %% "scala-logging" % scalaLoggingVersion,
    logbackOrg % "logback-classic" % logbackVersion
  )
}
