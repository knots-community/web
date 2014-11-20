package models.db

import models.db.TableDefinitions.Users
import utils.db.RichTable
import play.api.db.slick.Config.driver.simple._

/**
 * Created by anton on 11/16/14.
 */
object AuthTableDefinitions {

  case class LoginInfo(id: Option[Long], providerId: String, providerKey: String)
  class LoginInfos(tag: Tag) extends RichTable[LoginInfo](tag, "login_info") {
    def providerId = column[String]("provider_id")
    def providerKey = column[String]("provider_key")

    def * = (id.?, providerId, providerKey) <> (LoginInfo.tupled, LoginInfo.unapply)
  }

  case class UserLoginInfo(id: Option[Long], userId: Long, loginInfoId: Long)
  class UserLoginInfos(tag: Tag) extends RichTable[UserLoginInfo](tag, "user_login_info") {
    def userId = column[Long]("user_id", O.NotNull)
    def loginInfoId = column[Long]("login_info_id", O.NotNull)

    def loginInfoFk = foreignKey("login_info_fk", loginInfoId, TableQuery[LoginInfos])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

    def * = (id.?, userId, loginInfoId) <> (UserLoginInfo.tupled, UserLoginInfo.unapply)
  }

  case class TokenPassword(id: Option[Long], token: String, userId: Long)
  class TokenPasswords(tag: Tag) extends RichTable[TokenPassword](tag, "token_password") {
    def token = column[String]("token")
    def userId = column[Long]("user_id")

    def userFk = foreignKey("user_fk", userId, TableQuery[Users])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

    def * = (id.?, token, userId) <> (TokenPassword.tupled, TokenPassword.unapply)
  }

  case class PasswordInfo(id: Option[Long], hasher: String, password: String, salt: Option[String], loginInfoId: Long)
  class PasswordInfos(tag: Tag) extends RichTable[PasswordInfo](tag, "password_info") {
    def hasher = column[String]("hasher")
    def password = column[String]("password")
    def salt = column[Option[String]]("salt")
    def loginInfoId = column[Long]("login_info_id")

    def loginInfoFk = foreignKey("login_info_fk", loginInfoId, TableQuery[LoginInfos])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

    def * = (id.?, hasher, password, salt, loginInfoId) <> (PasswordInfo.tupled, PasswordInfo.unapply)
  }
}
