package models

import models.db.TableDefinitions._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.libs.json.Json

object Users extends Dao {
  implicit val jsonFormat = Json.format[User]

  def getAll = DB withSession { implicit session =>
    users.drop(1) list
  }

  def find(email: String, pwd: String): Option[User] = {
    DB withSession { implicit session =>
      val hash = PasswordHasher.hash(pwd)
      val q = for {
        pwdInfo <- tokenPasswords if pwdInfo.token === hash
        user <- users if user.id === pwdInfo.userId
      } yield user

      q.firstOption
    }
  }

  def findById(id: Long) : Option[User]= {
    DB withSession { implicit session =>
      users.filter(_.id === id).firstOption
    }
  }

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User, pwd: String) = {
    DB withTransaction { implicit session =>
      users.filter(_.email === user.email).firstOption.map(_ => None) getOrElse {
        val pid = Some(((users returning users.map(_.id)).insert(user)))
        tokenPasswords += TokenPassword(None, PasswordHasher.hash(pwd), pid.get)
        user.copy(id = pid)
      }
    }
  }

  def initialize() {
    DB withSession { implicit session =>
      if (users.list.length == 0) users += User(None, "", "", "")
      ()
    }
  }

}
