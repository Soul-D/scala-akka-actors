package at.co.sdt.herb.actors.doc.akka.io

import akka.actor.Actor

/** info extension for actors
  *
  */
trait ActorInfo {
  this: Actor =>

  /** name of current actor
    *
    * @return last element of actor path
    */
  protected def selfName: String = self.path.name
}
