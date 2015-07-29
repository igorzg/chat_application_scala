package actors

import akka.actor.{ActorLogging, ActorRef, Actor, Props}
import akka.event.LoggingReceive
import play.Logger
import scala.concurrent.Future
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current
import models.{User, UserDAO, Message, MessagesDAO}

/**
 * Event
 * @param name
 * @param id
 * @param params
 * @param terminate
 */
case class Event(name: String,
                 id: String,
                 params: JsValue,
                 terminate: Boolean,
                 session_id: String,
                 var out: Option[ActorRef])

/**
 * Event handler
 */
class EventHandler {
  /**
   * Lazy reads for recursive package
   */
  implicit lazy val userReads: Reads[User] = (
    (JsPath \ "user_id").readNullable[Int] and
      (JsPath \ "name").read[String] and
      (JsPath \ "session_id").readNullable[String]
    )(User.apply _)
  /**
   * Lazy write for recursive package config
   */
  implicit lazy val userWrites: Writes[User] = (
    (JsPath \ "name").writeNullable[Int] and
      (JsPath \ "name").write[String] and
      (JsPath \ "session_id").writeNullable[String]
    )(unlift(User.unapply))

  /**
   * Lazy reads for recursive package
   */
  implicit lazy val messageReads: Reads[Message] = (
    (JsPath \ "session_id").read[String] and
      (JsPath \ "user").read[String] and
      (JsPath \ "message").read[String]
    )(Message.apply _)
  /**
   * Lazy write for recursive package config
   */
  implicit lazy val messageWrites: Writes[Message] = (
    (JsPath \ "session_id").write[String] and
      (JsPath \ "user").write[String] and
      (JsPath \ "message").write[String]
    )(unlift(Message.unapply))

  val userDAO: UserDAO = new UserDAO
  val messageDAO: MessagesDAO = new MessagesDAO
  /**
   * Events list
   */
  var events: Array[Event] = Array()


  /**
   * Resolve user
   * @param out actor reference
   * @param event client event
   * @param user user record
   * @return
   */
  def resolveUser(out: ActorRef, event: Event)(user: Option[User]) = {
    var name: String = null
    if (user.isDefined) {
      name = user.get.name
    }
    out ! Json.obj(
      "name" -> event.name,
      "id" -> event.id,
      "data" -> Json.obj(
        "isLoggedIn" -> user.isDefined,
        "name" -> name
      )
    )
  }


  /**
   * Add an event
   * @param event
   */
  def add(event: Event) = {
    events = events :+ event
  }

  /**
   * Trigger event by name
   * @param name
   */
  def broadcast(name: String): Unit = {
    events.foreach(item => {
      if (item.name.equals(name)) {
        handle(item)
      }
    })
  }


  /**
   * Handle event
   * @param e
   */
  def handle(e: Event) = {
    if (has(e)) {
      val event = get(e)
      Logger.info("Handle {}", event.toString)
      event.name match {
        case "isUserLoggedIn" => {
          val f: Future[Seq[User]] = userDAO.getBySessionId(event.session_id)
          f onSuccess {
            case u: Seq[User] => {
              Logger.info("Users {}", u.toString)
              resolveUser(event.out.get, event)(u.headOption)
            }
            case _ => resolveUser(event.out.get, event)(null)
          }
        }
        case "logIn" => {
          val user: User = event.params.as[User]
          user.session_id = Option(event.session_id)
          Logger.info("user {}", user)
          userDAO.insert(user) onSuccess {
            case v: Unit => broadcast("isUserLoggedIn")
          }
        }
        case "logOut" => {
          userDAO.removeBySessionId(event.session_id) onSuccess {
            case v: Int => broadcast("isUserLoggedIn")
          }
        }
        case "unload" => {
          userDAO.removeBySessionId(event.session_id)
        }
        case "addMessage" => {
          val message: Message = event.params.as[Message]
          messageDAO.insert(message) onSuccess {
            case v: Unit => broadcast("allMessages")
          }
        }
        case "allMessages" => {
          Logger.info("Broadcast all {}", event.toString)
          messageDAO.all() onSuccess {
            case messages: Seq[Message] => {
              event.out.get ! Json.obj(
                "name" -> event.name,
                "id" -> event.id,
                "data" -> Json.toJson(messages)
              )
            }
          }
        }
      }
    }
  }

  /**
   * Clean all events by session id
   * @param session_id
   */
  def clean(session_id: String) = {
    events.filterNot(elm => elm.session_id == session_id)
  }

  /**
   * Has event
   * @param e
   * @return
   */
  def has(e: Event): Boolean = events.find(i => i.id == e.id).isDefined

  /**
   * Get an event
   * @param e
   * @return
   */
  def get(e: Event): Event = events.find(i => i.id == e.id).get

  /**
   * Remove event
   * @param e
   */
  def remove(e: Event): Unit = {
    if (has(e)) {
      events.filterNot(elm => elm == get(e))
    }
  }
}


/**
 * Transport actor
 * @param eventHandler
 */
class TransportActor(out: ActorRef, eventHandler: EventHandler) extends Actor with ActorLogging {
  /**
   * Format event
   */
  /**
   * Lazy reads for recursive package
   */
  implicit lazy val eventReads: Reads[Event] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "id").read[String] and
      (JsPath \ "params").read[JsValue] and
      (JsPath \ "terminate").read[Boolean] and
      (JsPath \ "session_id").read[String] and
      (JsPath \ "out").readNullable(null)
    )(Event.apply _)


  def receive = LoggingReceive {
    case json: JsValue => {
      val event: Event = json.as[Event]
      event.out = Option(out)
      if (event.name == "unload") {
        eventHandler.handle(event)
        eventHandler.clean(event.session_id)
      } else {
        if (event.terminate) {
          eventHandler.remove(event)
        } else if (!eventHandler.has(event)) {
          eventHandler.add(event)
        }
        eventHandler.handle(event)
      }

    }
    case _ => Logger.error("Transport actor received wrong type")
  }
}

/**
 * Transport actor
 */
object TransportActor {

  def props(eventHandler: EventHandler)(out: ActorRef) =
    Props(new TransportActor(out: ActorRef, eventHandler))
}