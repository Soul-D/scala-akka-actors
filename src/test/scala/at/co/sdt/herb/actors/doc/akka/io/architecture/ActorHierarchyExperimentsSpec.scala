package at.co.sdt.herb.actors.doc.akka.io.architecture

import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }

import akka.actor.{ ActorIdentity, ActorSelection, ActorSystem, Identify }
import akka.testkit.{ TestKit, TestProbe }

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

  val firstName = "first-actor"
  val secondName = "second-actor"

  "There" should "be no second-actor upon start" in {
    val firstActor = system.actorOf(PrintMyActorRefActor.props, firstName)
    val secondSel = ActorSelection(firstActor, secondName)

    // there should be no actor named "second-actor"
    val id = 1
    val probe = TestProbe()
    secondSel.tell(Identify(id), probe.ref)
    probe.expectNoMsg(500.milliseconds)
  }

  "A PrintMyActorRefActor" should "create an empty actor silently upon receiving a PrintIt message" in {
    val firstActor = system.actorOf(PrintMyActorRefActor.props, firstName)
    val secondSel = ActorSelection(firstActor, secondName)

    val probe = TestProbe()
    // firstActor must not respond
    firstActor.tell(PrintIt, probe.ref)
    probe.expectNoMsg(500.milliseconds)

    // there should be an actor with path "first-actor/second-actor"
    val id2 = 2
    val secondPath = s"$firstName/$secondName"
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
