package controllers

import scala.concurrent.Future
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import models._

object Transport extends Controller {


  val userDao: UserDAO = new UserDAO()

  def command(name: String) = Action.async {
      val user = new User(1, "Igor")
      val f = userDao.insert(user)
      Future.firstCompletedOf(Seq(f)).map {
        case user : Unit => Ok("User inserted")
      }
  }

  def query(name: String) = Action.async {
    val f = userDao.all()



    Future.firstCompletedOf(Seq(f)).map {
      case users : Seq[User] => Ok(users.toString)
    }
    /*
    name match {
      case "all" => Ok("test")
      case _ => BadRequest("This is bad request")
    }
  */
  }
}
