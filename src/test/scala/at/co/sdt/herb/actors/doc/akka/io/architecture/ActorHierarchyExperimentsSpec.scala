package at.co.sdt.herb.actors.doc.akka.io.architecture

import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }

import akka.actor.{ ActorIdentity, ActorSelection, ActorSystem, Identify, PoisonPill }
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

  // private val waitForStart: Long = 50
  private val waitForStop: Long = 25

  "A PrintMyActorRefActor" should "not start a \"second-actor\" upon start" in {
    val firstActor = system.actorOf(PrintMyActorRefActor.props, PrintMyActorRefActor.firstName)
    val secondSel = ActorSelection(firstActor, PrintMyActorRefActor.secondName)

    // there should be no actor named "second-actor"
    val id = 1
    val probe = TestProbe()
    secondSel.tell(Identify(id), probe.ref)
    probe.expectMsgType[ActorIdentity](500.milliseconds) should matchPattern {
      case ActorIdentity(`id`, None) =>
    }
    firstActor ! PoisonPill
    Thread.sleep(waitForStop)
  }

  it should "create a \"second-actor\" silently upon receiving a PrintIt message" in {
    val firstActor = system.actorOf(PrintMyActorRefActor.props, PrintMyActorRefActor.firstName)
    val secondSel = ActorSelection(firstActor, PrintMyActorRefActor.secondName)

    val probe = TestProbe()
    // firstActor must not respond
    firstActor.tell(PrintIt, probe.ref)
    probe.expectNoMsg(500.milliseconds)

    // there should be an actor with path "first-actor/second-actor"
    val id2 = 2
    secondSel.tell(Identify(id2), probe.ref)
    probe.expectMsgType[ActorIdentity](500.milliseconds) should matchPattern {
      case ActorIdentity(`id2`, Some(actorRef))
        if actorRef.path.parent == firstActor.path
          && actorRef.path.name == PrintMyActorRefActor.secondName =>
    }
    firstActor ! PoisonPill
    Thread.sleep(waitForStop)
  }
}
