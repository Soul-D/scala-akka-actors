package at.co.sdt.herb.actors.doc.akka.io.iot

import akka.actor.ActorSystem

import scala.io.StdIn

object IotApp extends App {

  val system = ActorSystem("iot-system")

  try {
    // Create top level supervisor
    system.actorOf(IotSupervisor.props(), "iot-supervisor")

    // Exit the system after ENTER is pressed
    StdIn.readLine()
  } finally {
    system.terminate()
  }
}