package at.co.sdt.herb.actors.doc.akka.io.architecture

import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }

import akka.actor.{ ActorIdentity, ActorRef, ActorSelection, ActorSystem, Identify, PoisonPill, Props }
import akka.testkit.{ TestKit, TestProbe }

import scala.concurrent.duration._

// test-classes
class LifeCycleSpec(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with FlatSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("FailureHandlingSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  private val waitForStart: Long = 50
  private val waitForStop: Long = 25

  "A StartStopActor1" should "create a second upon creation" in {
    val probe = TestProbe()
    val first = system.actorOf(Props[StartStopActor1], StartStopActor1.firstName)
    Thread.sleep(waitForStart)


    try {
      getSecond(first, probe) should matchPattern {
        case Some(_: ActorRef) =>
      }
    } finally {
      first ! PoisonPill
      Thread.sleep(waitForStop)
    }
  }

  it should "stop the second actor upon Stop" in {
    val probe = TestProbe()
    val first = system.actorOf(Props[StartStopActor1], StartStopActor1.firstName)
    Thread.sleep(waitForStart)

    try {
      val second = getSecond(first, probe).get

      // stop both on Stop
      probe.watch(second)
      first ! Stop
      probe.expectTerminated(second)
    } finally {
      first ! PoisonPill
      Thread.sleep(waitForStop)
    }
  }

  private def getSecond(first: ActorRef, probe: TestProbe): Option[ActorRef] = {
    val id2 = System.currentTimeMillis()
    val childSel = ActorSelection(first, StartStopActor1.secondName)
    childSel.tell(Identify(id2), probe.ref)
    val answer = probe.expectMsgType[ActorIdentity](500.milliseconds)
    // println(s"answer received: $answer")
    answer match {
      case ActorIdentity(`id2`, Some(child)) if child.path.toString contains StartStopActor1.secondName => Some(child)
      case m =>
        println(s"m $m received")
        None
    }
  }
}
