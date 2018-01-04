package at.co.sdt.herb.actors.doc.akka.io.futures

import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.testkit.{ TestKit, TestProbe }

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class FutureActorSpec(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with FlatSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("FutureSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  private val waitForStart: Long = 50
  private val waitForStop: Long = 25
  val probe = TestProbe()
  // implicit val timeout: Timeout = Timeout(FutureActor2.maxBlockingTime + (1 second))

  import FutureActor1._
  import FutureActor2._

  val actor2: ActorRef = system.actorOf(Props[FutureActor2], FutureActor2.name)
  Thread.sleep(waitForStart)
  val timeTolerance: FiniteDuration = 50 milliseconds

  "A FutureActor2" should "respond correctly to Immediate" in {
    tellActor2Immediate()
  }

  it should "respond correctly to NonBlocking" in {
    tellActor2NonBlocking(1 seconds)
    tellActor2NonBlocking(2 seconds)
  }

  it should "respond correctly to Blocking" in {
    tellActor2Blocking(1 seconds)
    tellActor2Blocking(2 seconds)
  }

  it should "answer correctly to Immediate" in {
    askActor2Immediate()
  }

  it should "answer correctly to NonBlocking" in {
    askActor2NonBlocking(1 seconds)
    askActor2NonBlocking(2 seconds)
  }

  it should "answer correctly to Blocking" in {
    askActor2Blocking(1 seconds)
    askActor2Blocking(2 seconds)
  }

  it should "timeout for too large times" in {
    askActor2Blocking(3 seconds)
    tellActor2Blocking(3 seconds)
  }

  val actor1: ActorRef = system.actorOf(Props[FutureActor1], FutureActor1.name)
  Thread.sleep(waitForStart)

  "A FutureActor1" should "respond correctly to non-blocking messages" in {

    val d0: FiniteDuration = 0 seconds;
    actor1.tell(Tell(Msg1(d0)), probe.ref)
    probe.expectMsg(d0 + timeTolerance, YouAreWelcome(Immediate))

    val d1: FiniteDuration = 1 seconds;
    actor1.tell(Tell(Msg1(d1)), probe.ref)
    probe.expectNoMessage(d1 - timeTolerance)
    probe.expectMsg(d1 + timeTolerance, YouAreWelcome(NonBlocking(d1)))

    val d2: FiniteDuration = 2 seconds;
    actor1.tell(Tell(Msg1(d2)), probe.ref)
    probe.expectNoMessage(d2 - timeTolerance)
    probe.expectMsg(d2 + timeTolerance, YouAreWelcome(NonBlocking(d2)))
  }

  it should "respond correctly to blocking messages" in {

    val d0: FiniteDuration = 0 seconds;
    actor1.tell(Tell(Msg1(d0)), probe.ref)
    probe.expectMsg(d0 + timeTolerance, YouAreWelcome(Immediate))

    val d1: FiniteDuration = 1 seconds;
    actor1.tell(Tell(Msg1(d1)), probe.ref)
    probe.expectNoMessage(d1 - timeTolerance)
    probe.expectMsg(d1 + timeTolerance, YouAreWelcome(NonBlocking(d1)))

    val d2: FiniteDuration = 2 seconds;
    actor1.tell(Tell(Msg1(d2)), probe.ref)
    probe.expectNoMessage(d2 - timeTolerance)
    probe.expectMsg(d2 + timeTolerance, YouAreWelcome(NonBlocking(d2)))
  }

  private def tellActor2Immediate(): Unit = {
    val m = Immediate
    actor2.tell(m, probe.ref)
    probe.expectMsg(timeTolerance, YouAreWelcome(m))
  }

  private def tellActor2NonBlocking(dur: FiniteDuration): Unit = {
    tellActor2(dur, NonBlocking(dur))
  }

  private def tellActor2Blocking(dur: FiniteDuration): Unit = {
    tellActor2(dur, Blocking(dur))
  }

  private def tellActor2(dur: FiniteDuration, msg: Msg2): Unit = {
    actor2.tell(msg, probe.ref)
    probe.expectNoMessage(dur - timeTolerance)
    if (dur > FutureActor2.maxBlockingTime)
      probe.expectNoMessage(dur + timeTolerance)
    else
      probe.expectMsg(dur + timeTolerance, YouAreWelcome(msg))
  }


  private def askActor2Immediate(): Unit = {
    askActor2(0 seconds, Immediate)
  }

  private def askActor2NonBlocking(dur: FiniteDuration): Unit = {
    askActor2(dur, NonBlocking(dur))
  }

  private def askActor2Blocking(dur: FiniteDuration): Unit = {
    askActor2(dur, Blocking(dur))
  }

  private def askActor2(dur: FiniteDuration, msg: Msg2): Unit = {
    val f = actor2.ask(msg)(dur + timeTolerance, probe.ref)
    if (dur > FutureActor2.maxBlockingTime)
      probe.expectNoMessage(dur + timeTolerance)
    else {
      val r = Await.result(f, dur + timeTolerance)
      r shouldBe YouAreWelcome(msg)
    }
  }

}