package actors

import akka.actor.{ActorLogging, ActorRef, Actor, Props}
import akka.event.LoggingReceive
import play.Logger
import play.api.libs.json.JsValue
import play.api.mvc.RequestHeader
import play.api.mvc.Session
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.cache.Cache

case class ResponseEvent(event: String, id: String, data: JsValue)
case class ReceiveEvent(event: String, id: String, params: JsValue)
case class User(nick: String)

object ReceiveEvent {
  implicit val formatReceiveEvent = Json.format[ReceiveEvent]
}
object ResponseEvent {
  implicit val formatResponseEvent = Json.format[ResponseEvent]
}
object User {
  implicit val formatUser = Json.format[User]
}

/**
 * Created by igi on 26/07/15.
 */
class UserActor(uid: String, request: RequestHeader, out: ActorRef) extends Actor with ActorLogging {

  def receive = LoggingReceive {
    case message: String => {
      val json: ReceiveEvent = Json.parse(message).as[ReceiveEvent]
      val keyPrefix = request.session.get("uid").getOrElse("0")
      val nickKey: String = keyPrefix + "_nick"
      json.event match {
        case "isUserLoggedIn" => {
          var nick : Option[String] = Cache.getAs[String](nickKey)
          var response: ResponseEvent = new ResponseEvent(json.event, json.id, Json.obj("isLoggedIn" -> !nick.isEmpty))
          val value: String = Json.stringify(Json.toJson(response))
          Logger.info("Sending event to client: nick {} and values {}", nick, value)
          out ! value
        }
        case "logIn" => {
          var u: User = json.params.as[User]
          Cache.set(nickKey, u.nick);
          Logger.info("Login event: {}", u.toString)
        }
      }

    }
    case _ => println("ERROR in useractor")
  }
}

object UserActor {
  def props(uid: String, request: RequestHeader)(out: ActorRef) = Props(new UserActor(uid, request, out))
}