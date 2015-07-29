package models

import play.api.Play
import play.api.libs.functional.syntax._
import play.api.libs.json.{Writes, JsPath, Reads}
import scala.concurrent.Future
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile
import play.Logger

/**
 * Created by igi on 29/07/15.
 */
case class Message (session_id: String, message: String, user: String)

trait MessageTable {
  self: HasDatabaseConfig[JdbcProfile] =>

  import driver.api._


  class MessageTable(tag: Tag) extends Table[Message](tag, "Message") {

    def user_id = column[String]("session_id")

    def user = column[String]("user")

    def message = column[String]("message")


    def * = (user_id, user, message) <>(Message.tupled, Message.unapply _)
  }

}


/**
 * User dao class
 */
class MessagesDAO extends MessageTable with HasDatabaseConfig[JdbcProfile] {

  protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  import driver.api._

  private lazy val messages = TableQuery[MessageTable]

  def all(): Future[Seq[Message]] = db.run(messages.result).map(_.toList)

  def insert(message: Message): Future[Unit] = db.run(messages += message).map(_ => ())

  def getByUserId(userId: String): Future[Seq[Message]] =
    db.run(messages.filter(m => m.user_id === userId).result)


}
