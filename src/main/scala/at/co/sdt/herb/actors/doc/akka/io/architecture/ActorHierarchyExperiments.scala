package at.co.sdt.herb.actors.doc.akka.io.architecture

import com.typesafe.scalalogging.{ LazyLogging, Logger }

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }

import at.co.sdt.herb.actors.doc.akka.io.ActorInfo

import scala.io.StdIn

case object PrintIt

case object PrintContext

object PrintMyActorRefActor {
  def props: Props = Props[PrintMyActorRefActor]

  val firstName = "first-actor"
  val secondName = "second-actor"
}

class PrintMyActorRefActor extends Actor with ActorInfo with ActorLogging {

  override def receive: Receive = {
    case PrintIt =>
      val secondRef = context.actorOf(Props.empty, PrintMyActorRefActor.secondName)
      log.debug(s"$selfName has created ${ secondRef.path.name }")
    case PrintContext =>
      val c = context // ist ein veränderlich!
      log.debug(s"$selfName context is $c")
      val s = sender() // ist nicht veränderlich
      log.debug(s"$selfName sender is $s")
      val parent = context.parent
      val children = context.children
      val child = context.child(PrintMyActorRefActor.secondName)
  }
}

object ActorHierarchyExperiments extends App with LazyLogging {
  val log: Logger = Logger(getClass.getSimpleName)
  log.trace("trace")
  log.debug("debug")
  log.info("info")
  log.warn("warning")
  log.error("error")

  val system = ActorSystem()

  val firstRef = system.actorOf(PrintMyActorRefActor.props, PrintMyActorRefActor.firstName)
  log.info(s"First: $firstRef")
  firstRef ! PrintIt

  println(">>> Press ENTER to exit <<<")
  try StdIn.readLine()
  finally system.terminate()
}