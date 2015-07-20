package controllers

import play.api._
import play.api.mvc._



object Transport extends Controller {

  def command(name: String) = Action {
    Ok("exec command");
  }

  def query(name: String) = Action {
    Ok("register query")
  }
}
