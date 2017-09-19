package at.co.sdt.herb.actors.doc.akka.io.architecture

import akka.actor.{ActorIdentity, ActorSelection, ActorSystem, Identify}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.duration._

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
    val firstActor = system.actorOf(PrintMyActorRefActor.props, "first-actor")
    val secondSel = ActorSelection(firstActor, "second-actor")

    // there should be no actor named "second-actor"
    val probe = TestProbe()
    secondSel.tell(PrintIt, probe.ref)
    probe.expectNoMsg(500.milliseconds)

    // firstActor must not respond
    firstActor.tell(PrintIt, probe.ref)
    probe.expectNoMsg(500.milliseconds)

    // there should be an actor with path "first-actor/second-actor"
    val id2 = 2
    val secondPath = "first-actor/second-actor"
    secondSel.tell(Identify(id2), probe.ref)
    probe.expectMsgType[ActorIdentity](500.milliseconds) should matchPattern {
      case ActorIdentity(`id2`, Some(actorRef)) if actorRef.path.toString contains secondPath =>
    }

    // TODO: a second actor named "second-actor" must not be started
    /* val firstId1 = firstActor.toString()
    firstActor.tell(PrintIt, probe.ref)
    val firstId2 = firstActor.toString()
    firstId1 should not be firstId2 of course the ActorRef is the same */
  }
}
