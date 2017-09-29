package at.co.sdt.herb.actors.doc.akka.io.architecture

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props }

import at.co.sdt.herb.actors.doc.akka.io.ActorInfo

case object Fail

case object FailChild

case object Stop

case object StopChild

object SupervisingActor {
  val name = "superVisor"
  val childName = "supervised-actor"
}

class SupervisingActor extends Actor with ActorLogging {
  val child: ActorRef = context.actorOf(Props[SupervisedActor], SupervisingActor.childName)
  log.debug(s"child created: $child")

  override def preStart(): Unit = log.debug("superVisor started")

  override def postStop(): Unit = log.debug("superVisor stopped")

  override def receive: Receive = {
    case Fail =>
      log.debug("superVisor fails now")
      throw new Exception("superVisor failed!")
    case FailChild => child ! Fail
    case Stop => context.stop(self)
    case StopChild => child ! PoisonPill
  }
}

class SupervisedActor extends Actor with ActorInfo with ActorLogging {
  override def preStart(): Unit = log.debug(s"$selfName started")

  override def postStop(): Unit = log.debug(s"$selfName stopped")

  override def receive: Receive = {
    case Fail =>
      log.debug(s"$selfName fails now")
      throw new Exception(s"$selfName failed!")
    case Stop => context.stop(self)
  }
}

object FailureHandling extends App {
  val system = ActorSystem()

  val superVisor = system.actorOf(Props[SupervisingActor], SupervisingActor.name)
  superVisor ! FailChild
  Thread.sleep(100L)
  superVisor ! Fail
  // superVisor ! Stop

  Thread.sleep(100L)
  println(">>> Press ENTER to exit <<<")
  /* try StdIn.readLine()
  finally */ system.terminate()
}
