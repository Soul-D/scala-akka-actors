package at.co.sdt.herb.actors.doc.akka.io.actors.FaultTolerance

import akka.actor.Actor

class Child extends Actor {
  var state = 0

  def receive: Receive = {
    case ex: Exception ⇒ throw ex
    case x: Int ⇒ state = x
    case "get" ⇒ sender() ! state
  }
}