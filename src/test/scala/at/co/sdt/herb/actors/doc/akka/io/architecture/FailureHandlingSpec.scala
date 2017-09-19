package at.co.sdt.herb.actors.doc.akka.io.architecture

import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }

import akka.actor.{ ActorIdentity, ActorRef, ActorSelection, ActorSystem, Identify, PoisonPill, Props }
import akka.testkit.{ TestKit, TestProbe }

import scala.concurrent.duration._

class FailureHandlingSpec(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with FlatSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("FailureHandlingSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  private val supervisorName = "superVisor"
  private val childName = "supervised-actor"
  private val waitForChildAfterSupervisor: Long = 25

  "A SupervisedActor" should "fail on Fail message" in {
    val probe = TestProbe()
    val supervised = system.actorOf(Props[SupervisedActor], childName)
    try {
      supervised ! Fail

      // Actor should restart
      val id = 3
      supervised.tell(Identify(id), probe.ref)
      probe.expectMsgType[ActorIdentity](500.milliseconds) should matchPattern {
        case ActorIdentity(`id`, Some(actorRef)) if actorRef == supervised =>
      }
    }
    finally {
      supervised ! PoisonPill
    }

  }

  it should "stop on Stop message" in {
    val probe = TestProbe()
    val supervised = system.actorOf(Props[SupervisedActor], childName)
    try {
      probe.watch(supervised)
      supervised ! Stop
      probe.expectTerminated(supervised)
    } finally {
      supervised ! PoisonPill
    }
  }

  "A SupervisingActor" should "throw an exception on Fail" in {
    val probe = TestProbe()
    val supervisor = system.actorOf(Props[SupervisingActor], supervisorName)

    try {
      // probe.watch(supervisor)
      supervisor ! Fail
      // Actor should restart by itself
      val id = 4
      supervisor.tell(Identify(id), probe.ref)
      probe.expectMsgType[ActorIdentity](500.milliseconds) should matchPattern {
        case ActorIdentity(`id`, Some(actorRef)) if actorRef == supervisor =>
      }
    } finally {
      supervisor ! PoisonPill
    }
  }

  it should "let child fail on FailChild" in {
    val probe = TestProbe()
    val supervisor = system.actorOf(Props[SupervisingActor], supervisorName)

    try {
      val child = getChild(supervisor, probe).get
      supervisor ! FailChild
      // child should restart itself
      val id = 5
      child.tell(Identify(id), probe.ref)
      probe.expectMsgType[ActorIdentity](500.milliseconds) should matchPattern {
        case ActorIdentity(`id`, Some(actorRef)) if actorRef == child =>
      }
    } finally {
      supervisor ! PoisonPill
    }
  }

  it should "stop on Stop" in {
    val probe1 = TestProbe()
    val probe2 = TestProbe()
    val supervisor = system.actorOf(Props[SupervisingActor], supervisorName)
    Thread.sleep(waitForChildAfterSupervisor)

    try {
      val child = getChild(supervisor, probe2).get

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

  it should "stop child on StopChild" in {
    val probe = TestProbe()
    val supervisor = system.actorOf(Props[SupervisingActor], supervisorName)
    Thread.sleep(waitForChildAfterSupervisor)

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
    val childSel = ActorSelection(supervisor, childName)
    childSel.tell(Identify(id2), probe.ref)
    val answer = probe.expectMsgType[ActorIdentity](500.milliseconds)
    println(s"answer received: $answer")
    answer match {
      case ActorIdentity(`id2`, Some(child)) if child.path.toString contains childName => Some(child)
      case m =>
        println(s"m $m received")
        None
    }
  }
}
