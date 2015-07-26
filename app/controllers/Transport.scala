package controllers

import scala.concurrent.Future
import play.api.mvc.Action
import play.api.mvc.Controller
import scala.util.{Success, Failure}
import models._

object Transport extends Controller {


  val userDao: UserDAO = new UserDAO()

  def command(name: String) = Action {
    var user = new User(1, "Igor")
    name match {
      case "create" => {
        userDao.insert(user)
        Ok("TEST")
      }
      case _ => BadRequest("This is bad request")
    }

  }

  def query(name: String) = Action {
    val f : Future[Seq[User]] = userDao.all()
    //f onSuccess
    name match {
      case "all" => Ok("test")
      case _ => BadRequest("This is bad request")
    }

  }
}
