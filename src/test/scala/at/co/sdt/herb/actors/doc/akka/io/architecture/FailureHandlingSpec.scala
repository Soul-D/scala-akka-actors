package at.co.sdt.herb.actors.doc.akka.io.architecture

import akka.actor.{ActorIdentity, ActorRef, ActorSelection, ActorSystem, Identify, PoisonPill, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.duration._

class FailureHandlingSpec(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with FlatSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("Spec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "A SupervisedActor" should "fail on Fail message" ignore {
    val probe = TestProbe()
    val supervised = system.actorOf(Props[SupervisedActor], "supervised")
    try {
      supervised ! Fail

      // Actor should restart
      val id2 = 3
      supervised.tell(Identify(id2), probe.ref)
      probe.expectMsgType[ActorIdentity](500.milliseconds) should matchPattern {
        case ActorIdentity(`id2`, Some(actorRef)) if actorRef == supervised =>
      }
    }
    finally {
      supervised ! PoisonPill
    }

  }

  it should "stop on Stop message" ignore {
    val probe = TestProbe()
    val supervised = system.actorOf(Props[SupervisedActor], "supervised")
    try {
      probe.watch(supervised)
      supervised ! Stop
      probe.expectTerminated(supervised)
    } finally {
      supervised ! PoisonPill
    }
  }

  "A SupervisingActor" should "throw an exception on Fail" ignore {
    val probe = TestProbe()
    val supervisor = system.actorOf(Props[SupervisingActor], "supervisor")

    try {
      ???
    } finally {
      supervisor ! PoisonPill
    }
  }

  it should "let child fail on FailChild" ignore {
    val probe = TestProbe()
    val supervisor = system.actorOf(Props[SupervisingActor], "supervisor")

    try {
      ???
    } finally {
      supervisor ! PoisonPill
    }
  }

  it should "stop on Stop" in {
    val probe1 = TestProbe()
    val probe2 = TestProbe()
    val supervisor = system.actorOf(Props[SupervisingActor], "supervisor")

    try {
      val child = getChild(supervisor, probe1).get

      // stop both on Stop
      probe1.watch(supervisor)
      probe2.watch(child)
      supervisor ! Stop
      probe1.expectTerminated(supervisor)
      probe2.expectTerminated(child)
    } finally {
      supervisor ! PoisonPill
    }
  }

  Thread.sleep(100)

  it should "stop child on StopChild" in {
    val probe = TestProbe()
    val supervisor = system.actorOf(Props[SupervisingActor], "supervisor")

    try {
      val child = getChild(supervisor, probe).get
      probe.watch(child)
      child ! Stop
      probe.expectTerminated(child)
    } finally {
      supervisor ! PoisonPill
    }
  }

  private def getChild(supervisor: ActorRef, probe: TestProbe): Option[ActorRef] = {
    val id2 = System.currentTimeMillis()
    val childPath = "supervised-actor"
    val childSel = ActorSelection(supervisor, childPath)
    childSel.tell(Identify(id2), probe.ref)
    val answer = probe.expectMsgType[ActorIdentity](500.milliseconds)
    println(s"answer received: $answer")
    answer match {
      case ActorIdentity(`id2`, Some(child)) if child.path.toString contains childPath => Some(child)
      case m =>
        println(s"m $m received")
        None
    }
  }
}
