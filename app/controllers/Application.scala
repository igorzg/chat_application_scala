package controllers

import play.Logger
import play.api._
import play.api.mvc._
import play.api.libs.Codecs.sha1

object Application extends Controller {

  val UID = "session_id"

  def index = Action { implicit request =>
    val sesionCookie: Option[Cookie] = request.cookies.get(UID)
    var sesion_id : String = null
    if (sesionCookie.isDefined) {
      sesion_id = sesionCookie.get.value
    } else {
      sesion_id = sha1(request.session + UID + Math.random().toString)
      Logger.info("set user session {}", sesion_id)
    }
    Ok(views.html.index(request, sesion_id)).withCookies(Cookie("session_id", sesion_id))
  }

}
