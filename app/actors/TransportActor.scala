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
import models.{User, UserDAO}

/**
 * Event
 * @param name
 * @param id
 * @param params
 * @param terminate
 */
case class Event(name: String, id: String, params: JsValue, terminate: Boolean, session_id: String)

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

  val userDAO: UserDAO = new UserDAO
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
  def broadcast(out: ActorRef, name: String): Unit = {
    events.foreach(item => {
      if (item.name.equals(name)) {
        handle(out, item)
      }
    })
  }


  /**
   * Handle event
   * @param e
   */
  def handle(out: ActorRef, e: Event) = {
    if (has(e)) {
      val event = get(e)
      Logger.info("Handle {}", event.toString)
      event.name match {
        case "isUserLoggedIn" => {
          val f : Future[Seq[User]] = userDAO.getBySessionId(event.session_id)
          f onSuccess {
            case u: Seq[User] => {
              Logger.info("Users {}", u.toString)
              resolveUser(out, event)(u.headOption)
            }
            case _ => resolveUser(out, event)(null)
          }
        }
        case "logIn" => {
          val user: User = event.params.as[User]
          user.session_id = Option(event.session_id)
          Logger.info("user {}", user)
          userDAO.insert(user) onSuccess {
            case v: Unit => broadcast(out, "isUserLoggedIn")
          }
        }
        case "logOut" => {
          userDAO.removeBySessionId(event.session_id) onSuccess {
            case v : Int => broadcast(out, "isUserLoggedIn")
          }
        }
        case "unload" => {
          userDAO.removeBySessionId(event.session_id)
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
  implicit val formatReceiveEvent = Json.format[Event]

  def receive = LoggingReceive {
    case json: JsValue => {
      val event: Event = json.as[Event]
      if (event.name == "unload") {
        eventHandler.handle(out, event)
        eventHandler.clean(event.session_id)
      } else {
        if (event.terminate) {
          eventHandler.remove(event)
        } else if (!eventHandler.has(event)) {
          eventHandler.add(event)
        }
        eventHandler.handle(out, event)
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