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

  private val firstName = "first"
  private val secondName = "second"

  "A StartStopActor1" should "create a second upon creation" in {
    val probe = TestProbe()
    val first = system.actorOf(Props[StartStopActor1], firstName)
    Thread.sleep(50)

    try {
      getSecond(first, probe) match {
        case Some(_: ActorRef) =>
        case _ => assert(false, s"$secondName not available")
      }
    } finally {
      first ! PoisonPill
    }
  }

  it should "stop the second actor upon Stop" in {
    val probe = TestProbe()
    val first = system.actorOf(Props[StartStopActor1], firstName)
    Thread.sleep(50)

    try {
      val second = getSecond(first, probe).get

      // stop both on Stop
      probe.watch(second)
      first ! Stop
      probe.expectTerminated(second)
    } finally {
      first ! PoisonPill
    }
  }

  private def getSecond(first: ActorRef, probe: TestProbe): Option[ActorRef] = {
    val id2 = System.currentTimeMillis()
    val childSel = ActorSelection(first, secondName)
    childSel.tell(Identify(id2), probe.ref)
    val answer = probe.expectMsgType[ActorIdentity](500.milliseconds)
    // println(s"answer received: $answer")
    answer match {
      case ActorIdentity(`id2`, Some(child)) if child.path.toString contains secondName => Some(child)
      case m =>
        println(s"m $m received")
        None
    }
  }
}
