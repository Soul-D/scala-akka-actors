package at.co.sdt.herb.actors.doc.akka.io.architecture

import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }

import akka.actor.ActorSystem
import akka.testkit.{ TestKit, TestProbe }

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
    val probe = TestProbe()
    val firstActor = system.actorOf(PrintMyActorRefActor.props, "first-actor")

    // TODO: there should be no actor named "second-actor"

    firstActor.tell(PrintIt, probe.ref)
    // an actor, who does not respond with a message ist not testable

    // TODO: there should be an actor named "second-actor"

    // TODO: there should not be a second actor named "second-actor"
    firstActor.tell(PrintIt, probe.ref)

  }
}
