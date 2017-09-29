package at.co.sdt.herb.actors.doc.akka.io.quickstart

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }

import scala.io.StdIn

/** printer-companion
  *
  */
object Printer {
  def props: Props = Props[Printer]

  /** printer-messages
    *
    * @param greeting String used for logging
    */
  final case class Greeting(greeting: String)

}

/** printer-actor
  *
  */
class Printer extends Actor with ActorLogging {

  import Printer._

  def receive: Receive = {
    case Greeting(greeting) =>
      log.debug(s"Greeting received (from ${ sender() }): $greeting")
  }
}

/** greeter-companion
  *
  */
object Greeter {
  def props(message: String, printerActor: ActorRef): Props = Props(classOf[Greeter], message, printerActor)

  // greeter-messages
  final case class WhoToGreet(who: String)

  case object Greet

}

/** greeter-actor
  *
  * @param message      message used for greeting
  * @param printerActor ActorRef used for greeting
  */
class Greeter(message: String, printerActor: ActorRef) extends Actor {

  import Greeter._
  import Printer._

  var greeting = ""

  def receive: Receive = {
    case WhoToGreet(who) =>
      // compose greeting message
      greeting = s"$message, $who"
    case Greet =>
      // send greeting message
      printerActor ! Greeting(greeting)
  }
}

/** main-class
  *
  */
object AkkaQuickstart extends App {

  import Greeter._

  // Create the 'helloAkka' actor system
  val system: ActorSystem = ActorSystem("helloAkka")

  try {
    // Create the printer actor
    val printer: ActorRef = system.actorOf(Printer.props, "defaultPrinter")

    // Create the 'greeter' actors
    val howdyGreeter: ActorRef =
      system.actorOf(Greeter.props("Howdy", printer), "howdyGreeter")
    val helloGreeter: ActorRef =
      system.actorOf(Greeter.props("Hello", printer), "helloGreeter")
    val goodDayGreeter: ActorRef =
      system.actorOf(Greeter.props("Good day", printer), "goodDayGreeter")

    // main-send-messages
    howdyGreeter ! WhoToGreet("Akka")
    howdyGreeter ! Greet

    howdyGreeter ! WhoToGreet("Lightbend")
    howdyGreeter ! Greet

    helloGreeter ! WhoToGreet("Scala")
    helloGreeter ! Greet

    goodDayGreeter ! WhoToGreet("Play")
    goodDayGreeter ! Greet

    println(">>> Press ENTER to exit <<<")
    StdIn.readLine()
  } finally {
    system.terminate()
  }
}
