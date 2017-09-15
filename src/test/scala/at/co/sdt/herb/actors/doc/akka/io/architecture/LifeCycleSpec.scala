package at.co.sdt.herb.actors.doc.akka.io.architecture

import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }

import akka.actor.ActorSystem
import akka.testkit.TestKit

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

  "A StartStopActor1" should "?" in {
    // TODO: implement test methods
    assert(false, "not yet implemented")
  }

  "A StartStopActor2" should "?" in {
    // TODO: implement test methods
    assert(false, "not yet implemented")
  }

}
