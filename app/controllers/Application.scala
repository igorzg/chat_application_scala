package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  val UID = "uid"
  var counter = 0

  def index = Action { implicit request =>
    val uid: String = request.session.get(UID).getOrElse {
      counter += 1
      counter.toString
    }
    Ok(views.html.index(request)).withSession(request.session + (UID -> uid))
  }

}
