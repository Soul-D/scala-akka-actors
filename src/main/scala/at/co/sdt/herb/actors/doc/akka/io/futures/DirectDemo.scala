package at.co.sdt.herb.actors.doc.akka.io.futures

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object DirectDemo extends App
  with LazyLogging {

  val future = Future {
    "Hello" + "World"
  }

  /* println(1)
  future foreach println

  val f: Future[String] = for {
    f <- future
  } yield f
  f onComplete {
    case Success(s) =>
      println(s)
    case t => println(t)
  }
  println(2) */

  val futureList = for (i <- 1L to 100000L) yield Future(i)
  val futureSum = Future.reduceLeft(futureList)(_ + _)
  for {r <- futureSum} println(s"future $r")

  // sum numbers from 1 to 100
  val list = for (i: Long <- 1L to 100000L) yield i // Future(i)
  println(s"direct ${ list.sum }")
  // Thread.sleep(100L)
}
