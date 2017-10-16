package at.co.sdt.herb.actors.doc.akka.io.general_concepts

import com.typesafe.scalalogging.{ LazyLogging, Logger }

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.io.StdIn
import scala.language.postfixOps


object ExpensiveCalculation {
  def props(echoActor: ActorRef, cleanUpActor: ActorRef): Props =
    Props(classOf[ExpensiveCalculation], echoActor, cleanUpActor)

  val name = "ExpCalc"

  case class BadMessage(msg: String)

  case class GoodMessage(msg: String)

}

class ExpensiveCalculation(echoActor: ActorRef, cleanUpActor: ActorRef) extends Actor with ActorLogging {

  implicit val ec: ExecutionContextExecutor = context.dispatcher
  implicit val timeout: Timeout = Timeout(5 seconds) // needed for `?` below

  def expensiveCalculation(actorRef: ActorRef, m: Any): String = {
    // this is a very costly operation
    log.info(s"answer to $m")
    "Meaning of life is 42"
  }

  def receive: Receive = {
    case m @ ExpensiveCalculation.BadMessage =>
      // Very bad: "sender" changes for every message,
      // shared mutable state bug
      Future { expensiveCalculation(sender(), m) }
    case m @ ExpensiveCalculation.GoodMessage =>
      // Completely safe: we close over a fixed value
      // and it's an ActorRef, which is thread-safe
      val currentSender = sender()
      Future { expensiveCalculation(currentSender, m) }
  }
}


object ExpCalcDemo extends App with LazyLogging {
  val log: Logger = Logger(getClass.getSimpleName)

  import ExpensiveCalculation._

  val system = ActorSystem()
  val expCalc = system.actorOf(props(null, null), name)
  log.info(s"$name: $expCalc")
  for (_ <- 1 to 5) {
    expCalc ! BadMessage
    expCalc ! GoodMessage
  }

  println(">>> Press ENTER to exit <<<")
  try StdIn.readLine()
  finally system.terminate()
}