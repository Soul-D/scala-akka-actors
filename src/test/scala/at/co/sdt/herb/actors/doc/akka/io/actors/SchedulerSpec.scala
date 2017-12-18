package at.co.sdt.herb.actors.doc.akka.io.actors

import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }

import akka.actor.{ ActorIdentity, ActorSystem, Identify, PoisonPill, Props }
import akka.testkit.{ TestKit, TestProbe }

import scala.concurrent.duration._

class SchedulerSpec(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with FlatSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("SchedulerSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  private val waitForStart: Long = 50
  private val waitForStop: Long = 25

  "A Scheduler" should "create two timers after 500 ms" in {
    val scheduler = system.actorOf(Props[Scheduler], Scheduler.name)
    Thread.sleep(waitForStart)

    val id = 1
    val probe = TestProbe()
    scheduler.tell(Identify(id), probe.ref)
    probe.expectMsgType[ActorIdentity](500.milliseconds) should matchPattern {
      case ActorIdentity(`id`, Some(actorRef))
        if actorRef.path == scheduler.path =>
    }

    scheduler ! PoisonPill
    Thread.sleep(waitForStop)
  }

  it should "stop by itself after 6 seconds" in {
    val probe = TestProbe()
    val scheduler = system.actorOf(Props[Scheduler], Scheduler.name)
    Thread.sleep(waitForStart)

    probe.watch(scheduler)
    probe.expectTerminated(scheduler, 6.seconds)
  }

  it should "stop upon LastTick" in {
    val probe = TestProbe()
    val scheduler = system.actorOf(Props[Scheduler], Scheduler.name)
    Thread.sleep(waitForStart)

    probe.watch(scheduler)
    scheduler ! Scheduler.LastTick
    probe.expectTerminated(scheduler)
  }
}
