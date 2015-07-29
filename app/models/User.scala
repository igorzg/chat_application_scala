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


case class User(var user_id: Option[Int] = null, name: String, var session_id: Option[String] = null)

/**
 * User table
 */
trait UserTable {
  self: HasDatabaseConfig[JdbcProfile] =>

  import driver.api._

  class UserTable(tag: Tag) extends Table[User](tag, "User") {

    def user_id = column[Option[Int]]("user_id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def session_id = column[Option[String]]("session_id", O.NotNull)

    def * = (user_id, name, session_id) <>(User.tupled, User.unapply _)
  }

}

/**
 * User dao class
 */
class UserDAO extends UserTable with HasDatabaseConfig[JdbcProfile] {

  protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  import driver.api._

  private lazy val users = TableQuery[UserTable]

  def all(): Future[Seq[User]] = db.run(users.result).map(_.toList)

  def insert(user: User): Future[Unit] = db.run(users += user).map(_ => ())

  def getBySessionId(session_id: String): Future[Seq[User]] =
    db.run(users.filter(u => u.session_id === Option(session_id)).result)


  def removeBySessionId(session_id: String) : Future[Int] = {
    val q = users.filter(_.session_id === Option(session_id))
    val action = q.delete
    db.run(action)
  }

}

