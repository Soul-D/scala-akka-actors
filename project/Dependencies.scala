import sbt._

object Dependencies {
  val akkaVersion = "2.5.4"

  val akkaOrg = "com.typesafe.akka"

  val deps = Seq(
    akkaOrg %% "akka-actor" % akkaVersion,
    akkaOrg %% "akka-testkit" % akkaVersion,
    "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  )
}
