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
      val q = for {
        user <- users if user.email === email
        pwdInfo <- tokenPasswords if pwdInfo.userId === user.id
      } yield (user, pwdInfo)

      val res: Option[(User, TokenPassword)] = q.firstOption
      res.map(tuple => if(PasswordHasher.matches(tuple._2.token, pwd)) { Some(tuple._1)} else None) getOrElse(None)
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
  def save(user: User, pwd: String) : Option[User] = {
    DB withTransaction { implicit session =>
      users.filter(_.email === user.email).firstOption.map(_ => None) getOrElse {
        val pid = Some(((users returning users.map(_.id)).insert(user)))
        tokenPasswords += TokenPassword(None, PasswordHasher.hash(pwd), pid.get)
        Some(user.copy(id = pid))
      }
    }
  }

  def initialize() {
    DB withSession { implicit session =>
      if (users.list.length == 0) users += User(None, "", "", "", 1)
      ()
    }
  }

}
