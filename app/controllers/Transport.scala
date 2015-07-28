package controllers

import play.Logger
import scala.Right
import scala.concurrent.Future
import play.api.mvc.Controller
import play.api.Play.current
import play.api.mvc.WebSocket
import play.api.libs.json.JsValue
import actors._

object Transport extends Controller {

  var eventHandler = new EventHandler()

  /**
   * Transport socket
   * @return
   */
  def transport = WebSocket.tryAcceptWithActor[JsValue, JsValue] { request =>
    Logger.info("Events {}", eventHandler.events.toString)
    Future.successful(Right(TransportActor.props(eventHandler)))
  }
}
