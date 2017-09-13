package at.co.sdt.herb.actors.doc.akka.io.architecture

import akka.actor.{ Actor, ActorRef, ActorSystem, PoisonPill, Props }

case object Fail

case object FailChild

case object Stop

case object StopChild

class SupervisingActor extends Actor {
  val child: ActorRef = context.actorOf(Props[SupervisedActor], "supervised-actor")

  override def preStart(): Unit = println("superVisor started")

  override def postStop(): Unit = println("superVisor stopped")

  override def receive: Receive = {
    case Fail =>
      println("superVisor fails now")
      throw new Exception("superVisor failed!")
    case FailChild => child ! Fail
    case Stop => context.stop(self)
    case StopChild => child ! PoisonPill
  }
}

class SupervisedActor extends Actor {
  override def preStart(): Unit = println("supervised actor started")

  override def postStop(): Unit = println("supervised actor stopped")

  override def receive: Receive = {
    case Fail =>
      println("supervised actor fails now")
      throw new Exception("supervised actor failed!")
    case Stop => context.stop(self)
  }
}

object FailureHandling extends App {
  val system = ActorSystem()

  val superVisor = system.actorOf(Props[SupervisingActor], "superVisor")
  superVisor ! FailChild
  Thread.sleep(100L)
  superVisor ! Fail
  // superVisor ! Stop

  Thread.sleep(100L)
  println(">>> Press ENTER to exit <<<")
  /* try StdIn.readLine()
  finally */ system.terminate()
}
