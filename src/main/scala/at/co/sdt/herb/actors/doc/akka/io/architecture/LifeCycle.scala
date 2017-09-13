package at.co.sdt.herb.actors.doc.akka.io.architecture

import akka.actor.{ Actor, ActorSystem, Props }

import scala.io.StdIn

class StartStopActor1 extends Actor {
  override def preStart(): Unit = {
    println("first started")
    context.actorOf(Props[StartStopActor2], "second")
  }

  override def postStop(): Unit = println("first stopped")

  override def receive: Receive = {
    case Stop => context.stop(self)
  }
}

class StartStopActor2 extends Actor {
  override def preStart(): Unit = println("second started")

  override def postStop(): Unit = println("second stopped")

  // Actor.emptyBehavior is a useful placeholder when we don't
  // want to handle any messages in the actor.
  override def receive: Receive = Actor.emptyBehavior
}

object LifeCycle extends App {
  val system = ActorSystem()

  val first = system.actorOf(Props[StartStopActor1], "first")
  first ! Stop

  Thread.sleep(100L)
  println(">>> Press ENTER to exit <<<")
  try StdIn.readLine()
  finally system.terminate()
}
