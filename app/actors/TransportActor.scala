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


case class User(nick: String)

object User {
  implicit val formatUser = Json.format[User]
}


/**
 * Event
 * @param name
 * @param id
 * @param params
 * @param terminate
 */
case class Event(name: String, id: String, params: JsValue, terminate: Boolean, session_id: String)

object Event {
  implicit val formatReceiveEvent = Json.format[Event]
}

/**
 * Event handler
 */
class EventHandler() {
  var events: Array[Event] = Array()

  def add(event: Event) = {
    events = events :+ event
  }

  /**
   * Trigger event by name
   * @param name
   */
  def trigger(out: ActorRef, name: String): Unit = {
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
          val nick: Option[String] = Cache.getAs[String](event.session_id + "_nick")
          val value: JsValue = Json.obj(
            "name" -> event.name,
            "id" -> event.id,
            "data" -> Json.obj(
              "isLoggedIn" -> nick.isDefined,
              "name" -> nick.getOrElse(null)
            )
          )
          out ! value
        }
        case "logIn" => {
          val u: User = event.params.as[User]
          Cache.set(event.session_id + "_nick", u.nick);
          trigger(out, "isUserLoggedIn")
        }
        case "logOut" => {
          Cache.remove(event.session_id + "_nick")
          trigger(out, "isUserLoggedIn")
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

  def receive = LoggingReceive {
    case json: JsValue => {
      val event: Event = json.as[Event]
      if (event.name == "unload") {
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