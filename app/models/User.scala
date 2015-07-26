package models

import play.api.Play
import scala.concurrent.Future
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile


case class User(user_id: Int, name: String)

/**
 * User table
 */
trait UserTable {
  self: HasDatabaseConfig[JdbcProfile] =>

  import driver.api._

  class UserTable(tag: Tag) extends Table[User](tag, "User") {

    def user_id = column[Int]("user_id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def * = (user_id, name) <>(User.tupled, User.unapply _)
  }

}

/**
 * User dao class
 */
class UserDAO extends UserTable with HasDatabaseConfig[JdbcProfile] {

  protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  import driver.api._

  private val users = TableQuery[UserTable]

  def all(): Future[Seq[User]] = db.run(users.result).map(_.toList)

  def insert(user: User): Future[Unit] = db.run(users += user).map(_ => ())

}

