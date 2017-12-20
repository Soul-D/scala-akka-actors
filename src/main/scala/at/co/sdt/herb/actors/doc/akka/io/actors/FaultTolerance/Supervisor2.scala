package at.co.sdt.herb.actors.doc.akka.io.actors.FaultTolerance

import akka.actor.{ Actor, Props }

class Supervisor2 extends Actor {

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  import scala.concurrent.duration._

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
      case _: ArithmeticException ⇒ Resume
      case _: NullPointerException ⇒ Restart
      case _: IllegalArgumentException ⇒ Stop
      case _: Exception ⇒ Escalate
    }

  def receive: Receive = {
    case p: Props ⇒ sender() ! context.actorOf(p)
  }

  // override default to kill all children during restart
  override def preRestart(cause: Throwable, msg: Option[Any]) {}
}