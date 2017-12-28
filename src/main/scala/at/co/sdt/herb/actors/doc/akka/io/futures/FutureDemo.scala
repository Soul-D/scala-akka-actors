package at.co.sdt.herb.actors.doc.akka.io.futures

import com.typesafe.scalalogging.{ LazyLogging, Logger }

import akka.actor.{ ActorRef, ActorSystem }

import scala.concurrent.duration._
import scala.language.postfixOps

object FutureDemo extends App
  with LazyLogging {

  import FutureActor1._
  import FutureActor2._

  val system = ActorSystem("Futures")
  val actor1: ActorRef = system.actorOf(FutureActor1.props, "actor1")
  val log: Logger = Logger(getClass.getSimpleName)

  actor1 ! Msg1(3 seconds, blocking = true)
  actor1 ! Msg1(0 seconds)
  /* actor1 ! Msg1(1 seconds)
  actor1 ! Msg1(1 seconds, blocking = true) */

  val waitTime = maxBlockingTime + (2 seconds)
  log.info(s"Actorsystem $system will be shut down in $waitTime")
  Thread.sleep(waitTime.toMillis)
  system.terminate()
}
