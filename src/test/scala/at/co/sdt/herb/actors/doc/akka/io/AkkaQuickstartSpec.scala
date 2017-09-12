package at.co.sdt.herb.actors.doc.akka.io

import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }

import akka.actor.ActorSystem
import akka.testkit.{ TestKit, TestProbe }

import scala.concurrent.duration._

// test-classes
class AkkaQuickstartSpec(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with FlatSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("AkkaQuickstartSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  // pecification-example
  "A Greeter Actor" should "pass on a greeting message when instructed to" in {
    //#specification-example
    val testProbe = TestProbe()
    val helloGreetingMessage = "hello"
    val helloGreeter = system.actorOf(Greeter.props(helloGreetingMessage, testProbe.ref))
    val greetPerson = "Akka"
    helloGreeter ! Greeter.WhoToGreet(greetPerson)
    helloGreeter ! Greeter.Greet
    testProbe.expectMsg(500.millis, Printer.Greeting(s"$helloGreetingMessage, $greetPerson"))
  }
}
