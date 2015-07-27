package controllers

import scala.Left
import scala.Right
import scala.concurrent.Future
import play.api.mvc.Controller
import play.api.Play.current
import play.api.mvc.WebSocket
import actors._

object Transport extends Controller {


  def transport = WebSocket.tryAcceptWithActor[String, String] { request =>
    Future.successful(request.session.get("uid") match {
      case None => Left(Forbidden)
      case Some(uid) => Right(UserActor.props(uid, request))
    })
  }
}
