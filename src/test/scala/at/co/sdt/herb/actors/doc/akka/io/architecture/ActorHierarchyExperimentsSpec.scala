package at.co.sdt.herb.actors.doc.akka.io.architecture

import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }

import akka.actor.ActorSystem
import akka.testkit.TestKit

// test-classes
class ActorHierarchyExperimentsSpec(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with FlatSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("ActorHierarchyExperimentsSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "A PrintMyActorRefActor" should "create an empty actor upon receiving a PrintIt message" in {
    //#specification-example
    val firstMessage = PrintIt
    val firstActor = system.actorOf(PrintMyActorRefActor.props, "first-actor")

    firstActor ! firstMessage

    // TODO: implement test methods
  }
}
