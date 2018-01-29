package at.co.sdt.herb.actors.doc.akka.io.actors.FSM

import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

import akka.actor.{ ActorSystem, Props }
import akka.testkit.{ ImplicitSender, TestKit }
import akka.util.ByteString

import language.postfixOps
import scala.collection.immutable

object FSMDocSpec {
  // messages and data types
}

class FSMDocSpec extends TestKit(ActorSystem("FSMDocSpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  import akka.actor.FSM

  import scala.concurrent.duration._

  object DemoCode {

    trait StateType

    case object SomeState extends StateType

    case object Processing extends StateType

    case object Error extends StateType

    case object Idle extends StateType

    case object Active extends StateType

    class Dummy extends FSM[StateType, Int] {

      class X

      val newData = 42

      object WillDo

      object Tick

      when(SomeState) {
        //case Event(msg, _) ⇒
        case Event(_, _) ⇒
          goto(Processing) using newData forMax (5 seconds) replying WillDo
      }

      onTransition {
        case Idle -> Active ⇒ setTimer("timeout", Tick, 1 second, repeat = true)
        case Active -> _ ⇒ cancelTimer("timeout")
        case x -> Idle ⇒ log.info("entering Idle from " + x)
      }

      onTransition(handler _)

      def handler(from: StateType, to: StateType) {
        // handle it here ...
      }

      when(Error) {
        case Event("stop", _) ⇒
          // do cleanup ...
          stop()
      }

      when(SomeState)(transform {
        case Event(bytes: ByteString, read) ⇒ stay using (read + bytes.length)
      } using {
        // case s @ FSM.State(state, read, timeout, stopReason, replies) if read > 1000 ⇒
        case FSM.State(_, read, _, _, _) if read > 1000 ⇒
          goto(Processing)
      })

      val processingTrigger: PartialFunction[State, State] = {
        case FSM.State(_, read, _, _, _) if read > 1000 ⇒
          goto(Processing)
      }

      when(SomeState)(transform {
        case Event(bytes: ByteString, read) ⇒ stay using (read + bytes.length)
      } using processingTrigger)

      onTermination {
        // case StopEvent(FSM.Normal, state, data) ⇒ // ...
        case StopEvent(FSM.Normal, _, _) ⇒ // ...
        case StopEvent(FSM.Shutdown, _, _) ⇒ // ...
        // case StopEvent(FSM.Failure(cause), state, data) ⇒ // ...
        case StopEvent(FSM.Failure(_), _, _) ⇒ // ...
      }

      whenUnhandled {
        case Event(x: X, data) ⇒
          log.info(s"Received unhandled event: $x with $data")
          stay
        case Event(msg, _) ⇒
          log.warning(s"Received unknown event: $msg")
          goto(Error)
      }

    }

    import akka.actor.LoggingFSM

    class MyFSM extends LoggingFSM[StateType, Data] {
      override def logDepth = 12

      onTermination {
        case StopEvent(FSM.Failure(_), state, data) ⇒
          val lastEvents = getLog.mkString("\n\t")
          log.warning("Failure in state " + state + " with data " + data + "\n" +
            "Events leading up to this point:\n\t" + lastEvents)
      }
      // ...
    }

  }

  "simple finite state machine" must {

    "demonstrate NullFunction" in {
      class A extends FSM[Int, Null] {
        val SomeState = 0
        when(SomeState)(FSM.NullFunction)
      }
    }

    "batch correctly" in {
      val buncher = system.actorOf(Props[Buncher])
      buncher ! SetTarget(testActor)
      buncher ! Queue(42)
      buncher ! Queue(43)
      expectMsg(Batch(immutable.Seq(42, 43)))
      buncher ! Queue(44)
      buncher ! Flush
      buncher ! Queue(45)
      expectMsg(Batch(immutable.Seq(44)))
      expectMsg(Batch(immutable.Seq(45)))
    }

    "not batch if uninitialized" in {
      val buncher = system.actorOf(Props[Buncher])
      buncher ! Queue(42)
      expectNoMessage(1 second)
    }
  }
}