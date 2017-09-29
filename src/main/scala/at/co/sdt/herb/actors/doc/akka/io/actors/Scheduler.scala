package at.co.sdt.herb.actors.doc.akka.io.actors

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props, Timers }

import scala.io.StdIn


object Scheduler {

  private case object TickKey

  private case object FirstTick

  private case object Tick

  private case object LastTick

  def props: Props = Props(classOf[Scheduler])
}

class Scheduler extends Actor with Timers with ActorLogging {

  import Scheduler._

  import scala.concurrent.duration._

  var ticksReceived: Int = 0
  val maxTicksRecevied: Int = 5

  log.info(s"${ getClass.getSimpleName } started")
  timers.startSingleTimer(TickKey, FirstTick, 500.millis)

  override def receive: Receive = {
    case FirstTick =>
      timers.startPeriodicTimer(TickKey, Tick, 1.second)
      log.info(s"timer for 1 s started")
    case t @ Tick =>
      ticksReceived += 1
      log.info(s"$t received")
      if (ticksReceived >= maxTicksRecevied) {
        timers.cancel(TickKey)
        self ! LastTick
      }
    case l @ LastTick =>
      log.info(s"$l received")
      context.stop(self)
  }
}

object Demo extends App {
  val system = ActorSystem("Scheduler")
  val scheduler = system.actorOf(Scheduler.props, "scheduler1")

  // alternative system.scheduler.scheduleOnce()

  StdIn.readLine()
  system.terminate
}