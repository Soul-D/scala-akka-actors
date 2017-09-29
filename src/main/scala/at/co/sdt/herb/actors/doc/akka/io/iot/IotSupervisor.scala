package at.co.sdt.herb.actors.doc.akka.io.iot

import akka.actor.{ Actor, ActorLogging, Props }

object IotSupervisor {
  def props(): Props = Props[IotSupervisor]
}

class IotSupervisor extends Actor with ActorLogging {
  override def preStart(): Unit = log.debug("IoT Application started")

  override def postStop(): Unit = log.debug("IoT Application stopped")

  // No need to handle any messages
  override def receive: Receive = Actor.emptyBehavior

}
