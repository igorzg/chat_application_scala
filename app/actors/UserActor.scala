package actors

import akka.actor.{ActorLogging, ActorRef, Actor, Props}
import akka.event.LoggingReceive
import play.api.libs.json.JsValue

/**
 * Created by igi on 26/07/15.
 */
class UserActor(uid: String, out: ActorRef) extends Actor with ActorLogging{

  def receive = LoggingReceive {
    case js: JsValue => println("NEW MESSAGE:" + js.toString())
    case _ => println("ERROR in useractor")
  }
}

object UserActor {
  def props(uid: String)(out: ActorRef) = Props(new UserActor(uid, out))
}