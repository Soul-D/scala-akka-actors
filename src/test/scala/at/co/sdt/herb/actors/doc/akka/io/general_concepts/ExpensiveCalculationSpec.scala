package at.co.sdt.herb.actors.doc.akka.io.general_concepts

import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }

import akka.actor.{ ActorSystem, PoisonPill }
import akka.testkit.TestKit

class ExpensiveCalculationSpec(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with FlatSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("ExpensiveCalculationSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  private val waitForStart: Long = 50
  private val waitForStop: Long = 25

  "An ExpensiveCalculation actor" should "do sth." in {
    val expCalc = system.actorOf(ExpensiveCalculation.props(null, null), ExpensiveCalculation.name)
    Thread.sleep(waitForStart)
    expCalc ! PoisonPill
    Thread.sleep(waitForStop)
  }

}