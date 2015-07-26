package controllers

import controllers.Application
import scala.Left
import scala.Right
import scala.concurrent.Future
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import play.api.Play.current
import play.api.mvc.WebSocket
import actors._
import play.api.libs.json._
import models._

object Transport extends Controller {

  val UID = "uid"
  val userDao: UserDAO = new UserDAO()


  def command(name: String) = Action.async {
      val user = new User(1, "Igor")
      val f = userDao.insert(user)
      Future.firstCompletedOf(Seq(f)).map {
        case user : Unit => Ok("User inserted")
      }
  }

  def transport = WebSocket.tryAcceptWithActor[JsValue, JsValue] { implicit request =>
    Future.successful(request.session.get(UID) match {
      case None => Left(Forbidden)
      case Some(uid) => Right(UserActor.props(uid))
    })
  }
  /*
  def query(name: String) = Action.async {
    val f = userDao.all()
    Future.firstCompletedOf(Seq(f)).map {
      case users : Seq[User] => Ok(users.toString)
    }
  }
  */


}
