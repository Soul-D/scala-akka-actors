package at.co.sdt.herb.actors.doc.akka.io.architecture

import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }

import akka.actor.{ ActorSystem, Props }
import akka.testkit.{ TestKit, TestProbe }

// test-classes
class FailureHandlingSpec(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with FlatSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("FailureHandlingSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "A SupervisedActor" should "fail on Fail message" in {
    val probe = TestProbe()
    val supervised = system.actorOf(Props[SupervisedActor], "supervised-actor")

    // TODO: implement test method
    assert(false, "not yet implemented")
  }

  it should "stop on Stop message" in {
    val probe = TestProbe()
    val supervised = system.actorOf(Props[SupervisedActor], "supervised-actor")

    // TODO: implement test method
    assert(false, "not yet implemented")
  }

  "A SupervisingActor" should "?" in {
    val probe = TestProbe()
    val supervisor = system.actorOf(Props[SupervisingActor], "supervisor")

    // TODO: implement test methods
    // throw exception on Fail
    // let child fail on FailChild
    // stop on Stop
    // stop child on StopChild

    assert(false, "not yet implemented")
  }
}
