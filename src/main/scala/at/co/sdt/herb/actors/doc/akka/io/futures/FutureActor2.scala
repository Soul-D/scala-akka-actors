package at.co.sdt.herb.actors.doc.akka.io.futures

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{ Failure, Success }

object FutureActor2 {
  def props: Props = Props[FutureActor2]

  trait Msg2

  trait Msg2WithDuration extends Msg2 {
    def time: Duration
  }

  case object Immediate extends Msg2

  case class Blocking(time: Duration) extends Msg2WithDuration

  case class NonBlocking(time: Duration) extends Msg2WithDuration

  case class YouAreWelcome(msg: Msg2)

  val maxBlockingTime: FiniteDuration = 2 seconds

}

class FutureActor2 extends Actor with ActorLogging {

  // import context.dispatcher
  import FutureActor2._

  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {
    case i @ Immediate =>
      log.debug(s"answering ${ YouAreWelcome(i) } to ${ sender().path.name }")
      sender() ! YouAreWelcome(i)
    case b: Blocking => blockAndRespond(b, sender())
    case n: NonBlocking =>
      val lastSender = sender()
      val f = Future(blockAndRespond(n, lastSender))
      f.onComplete {
        case Success(_) => log.info(s"future $f successful")
        case Failure(t) =>
          log.error(s"future $f failed")
          throw t
      }
      log.debug(s"not responding to $n")
  }

  def blockExecution(d: Duration): Unit = {
    if (d > maxBlockingTime) throw new IllegalArgumentException(s"Blocking time must not exceed $maxBlockingTime")
    log.debug(s"blocking for ${ d }")
    Thread.sleep(d.toMillis)
  }

  def respond(m: Msg2, r: ActorRef): Unit = {
    log.debug(s"answering ${ YouAreWelcome(m) } to ${ r.path.name }")
    r ! YouAreWelcome(m)
  }

  def blockAndRespond(m: Msg2WithDuration, r: ActorRef): Unit = {
    blockExecution(m.time)
    respond(m, r)
  }
}
