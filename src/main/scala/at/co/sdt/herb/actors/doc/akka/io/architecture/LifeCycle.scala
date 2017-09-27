package at.co.sdt.herb.actors.doc.akka.io.architecture

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }

import at.co.sdt.herb.actors.doc.akka.io.ActorInfo

import scala.io.StdIn

object StartStopActor1 {
  val firstName = "first"
  val secondName = "second"
}


class StartStopActor1 extends Actor with ActorInfo with ActorLogging {

  override def preStart(): Unit = {

    log.info(s"$selfName started")
    context.actorOf(Props[StartStopActor2], StartStopActor1.secondName)
  }

  override def postStop(): Unit = log.debug(s"$selfName stopped")

  override def receive: Receive = {
    case Stop => context.stop(self)
  }
}

class StartStopActor2 extends Actor with ActorInfo with ActorLogging {

  override def preStart(): Unit = log.debug(s"$selfName started")

  override def postStop(): Unit = log.debug(s"$selfName stopped")

  // Actor.emptyBehavior is a useful placeholder when we don't
  // want to handle any messages in the actor.
  override def receive: Receive = Actor.emptyBehavior
}

object LifeCycle extends App {
  val system = ActorSystem()

  val first = system.actorOf(Props[StartStopActor1], StartStopActor1.firstName)
  first ! Stop

  Thread.sleep(100L)
  println(">>> Press ENTER to exit <<<")
  try StdIn.readLine()
  finally system.terminate()
}
