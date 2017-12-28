package at.co.sdt.herb.actors.doc.akka.io.futures

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{ Failure, Success }

object FutureActor1 {
  def props: Props = Props[FutureActor1]

  case class Msg1(duration: Duration, blocking: Boolean = false)

}

class FutureActor1 extends Actor with ActorLogging {

  import FutureActor1._
  import FutureActor2._

  val actor2: ActorRef = context.actorOf(FutureActor2.props, "actor2")

  override def receive: Receive = {
    case m: Msg1 => askActor(actor2, m)
  }

  def msg2(msg1: Msg1): Msg2 = {
    if (msg1.duration.toMillis > 0L) {
      if (msg1.blocking) Blocking(msg1.duration)
      else NonBlocking(msg1.duration)
    }
    else Immediate
  }

  def askActor(ref: ActorRef, msg: Msg1): Unit = {
    implicit val timeout: Timeout = Timeout(maxBlockingTime + (1 second))
    val m = msg2(msg)
    log.info(s"asking ${ ref.path.name } for $m")
    val f = ref ? m
    f.onComplete {
      case Success(a) => log.info(s"received $a")
      case Failure(t) =>
        // throw new IllegalArgumentException("development")
        log.error(s"$t")
      // throw t
    }
  }

}
