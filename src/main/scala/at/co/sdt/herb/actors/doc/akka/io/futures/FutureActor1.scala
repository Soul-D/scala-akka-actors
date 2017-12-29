package at.co.sdt.herb.actors.doc.akka.io.futures

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{ Failure, Success }

object FutureActor1 {
  def props: Props = Props[FutureActor1]

  val name = "actor1"

  case class Msg1(duration: Duration, blocking: Boolean = false)

  case class Ask(msg1: Msg1)

  case class Tell(msg1: Msg1)

}

class FutureActor1 extends Actor with ActorLogging {

  import FutureActor1._
  import FutureActor2._

  val actor2: ActorRef = context.actorOf(FutureActor2.props, "actor2")
  var lastTeller: ActorRef = context.system.deadLetters

  override def receive: Receive = {
    case Ask(m) =>
      val f = askActor(actor2, m)
      f.onComplete {
        case Success(s) =>
          log.info(s"${ actor2.path.name } answered $s")
        case Failure(t) =>
          // throw new IllegalArgumentException("development")
          log.error(s"future $f failed with $t")
          throw t
      }
    case Tell(m) =>
      lastTeller = sender()
      tellActor(actor2, m)
    case a @ YouAreWelcome(m) =>
      log.info(s"received $a from ${ sender().path.name }")
      lastTeller ! a
    case u =>
      log.info(s"unknown message $u from ${ sender().path }")
      lastTeller ! UnknownMessage(u)
  }

  def msg2(msg1: Msg1): Msg2 = {
    if (msg1.duration.toMillis > 0L) {
      if (msg1.blocking) Blocking(msg1.duration)
      else NonBlocking(msg1.duration)
    }
    else Immediate
  }

  def askActor(ref: ActorRef, msg: Msg1): Future[YouAreWelcome] = {
    implicit val timeout: Timeout = Timeout(maxBlockingTime + (1 second))
    val m = msg2(msg)
    log.info(s"asking ${ ref.path.name } for $m")
    (ref ? m).mapTo[YouAreWelcome]
  }

  def tellActor(ref: ActorRef, msg: Msg1): Unit = {
    val m = msg2(msg)
    log.info(s"telling $m to ${ ref.path.name }")
    ref ! m
  }
}
