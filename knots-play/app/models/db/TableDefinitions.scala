package models.db

import org.joda.time.DateTime
import play.api.db.slick.Config.driver.simple._
import utils.db.RichTable

object TableDefinitions {

  case class DbRole(id: Option[Long], name: String, description: Option[String])

  class Roles(tag: Tag) extends RichTable[DbRole](tag, "role") {
    def name = column[String]("name", O.NotNull)
    def description = column[Option[String]]("description", O.Nullable)

    def * = (id.?, name, description) <> (DbRole.tupled, DbRole.unapply)
    def idx = index("idx", (id, name), unique = true)
    def users = TableQuery[UserRoles].filter(_.roleId === id).flatMap(_.userFK)
  }

  case class DbUser(id: Option[Long], firstName: Option[String], lastName: Option[String], email: Option[String])

  class Users(tag: Tag) extends RichTable[DbUser](tag, "user") {
    def firstName = column[Option[String]]("firstName")
    def lastName = column[Option[String]]("lastName")
    def email = column[Option[String]]("email")
    def * = (id.?, firstName, lastName, email) <> (DbUser.tupled, DbUser.unapply)
    def roles = TableQuery[UserRoles].filter(_.userId === id).flatMap(_.roleFK)
  }

  case class DbUserRole(id: Option[Long], userId: Option[Long], roleId: Option[Long])

  class UserRoles(tag: Tag) extends RichTable[DbUserRole](tag, "user_role") {
    def userId = column[Long]("userId")
    def roleId = column[Long]("roleId")

    def userFK = foreignKey("user_fk", userId, TableQuery[Users])(_.id/*, onUpdate = ForeignKeyAction.Cascade*/)
    def roleFK = foreignKey("role_fk", roleId, TableQuery[Roles])(_.id/*, onUpdate = ForeignKeyAction.Restrict*/)

    def * = (id.?, userId.?, roleId.?) <> (DbUserRole.tupled, DbUserRole.unapply)
  }

  case class DbLoginInfo(id: Option[Long], providerID: String, providerKey: String)

  class LoginInfos(tag: Tag) extends RichTable[DbLoginInfo](tag, "logininfo") {
    def providerID = column[String]("providerID")
    def providerKey = column[String]("providerKey")
    def * = (id.?, providerID, providerKey) <> (DbLoginInfo.tupled, DbLoginInfo.unapply)
  }

  case class DbUserLoginInfo(id: Option[Long], userID: Long, loginInfoId: Long)

  class UserLoginInfos(tag: Tag) extends RichTable[DbUserLoginInfo](tag, "userlogininfo") {
    def userID = column[Long]("userID", O.NotNull)
    def loginInfoId = column[Long]("loginInfoId", O.NotNull)
    def * = (id.?, userID, loginInfoId) <> (DbUserLoginInfo.tupled, DbUserLoginInfo.unapply)
  }

  case class DbPasswordInfo(id: Option[Long], hasher: String, password: String, salt: Option[String], loginInfoId: Long)

  class PasswordInfos(tag: Tag) extends RichTable[DbPasswordInfo](tag, "passwordinfo") {
    def hasher = column[String]("hasher")
    def password = column[String]("password")
    def salt = column[Option[String]]("salt")
    def loginInfoId = column[Long]("loginInfoId")
    def * = (id.?, hasher, password, salt, loginInfoId) <> (DbPasswordInfo.tupled, DbPasswordInfo.unapply)
  }

  case class DbOAuth1Info(id: Option[Long], token: String, secret: String, loginInfoId: Long)

  class OAuth1Infos(tag: Tag) extends RichTable[DbOAuth1Info](tag, "oauth1info") {
    def token = column[String]("token")
    def secret = column[String]("secret")
    def loginInfoId = column[Long]("loginInfoId")
    def * = (id.?, token, secret, loginInfoId) <> (DbOAuth1Info.tupled, DbOAuth1Info.unapply)
  }

  case class DbOAuth2Info(id: Option[Long], accessToken: String, tokenType: Option[String], expiresIn: Option[Int],
                          refreshToken: Option[String], loginInfoId: Long)

  class OAuth2Infos(tag: Tag) extends RichTable[DbOAuth2Info](tag, "oauth2info") {
    def accessToken = column[String]("accesstoken")
    def tokenType = column[Option[String]]("tokentype")
    def expiresIn = column[Option[Int]]("expiresin")
    def refreshToken = column[Option[String]]("refreshtoken")
    def loginInfoId = column[Long]("logininfoid")
    def * = (id.?, accessToken, tokenType, expiresIn, refreshToken, loginInfoId) <> (DbOAuth2Info.tupled, DbOAuth2Info.unapply)
  }

  case class Admin(id: Option[Long], userId: Option[Long])

  class Admins(tag: Tag) extends RichTable[Admin](tag, "admins") {
    def userId = column[Long]("userId", O.NotNull)
    def user = foreignKey("user_fk", userId, TableQuery[Users])(_.id, onUpdate = ForeignKeyAction.Cascade)
    def * = (id.?, userId.?) <> (Admin.tupled, Admin.unapply)
  }

  case class Masseur(id: Option[Long], userId: Long, sex: String)

  class Masseurs(tag: Tag) extends RichTable[Masseur](tag, "masseurs") {
    def userId = column[Long]("userId", O.NotNull)
    def sex = column[String]("sex", O.NotNull)

    def user = foreignKey("user_fk", userId, TableQuery[Users])(_.id, onUpdate = ForeignKeyAction.Cascade)

    def * = (id.?, userId, sex) <> (Masseur.tupled, Masseur.unapply)
  }

  case class MassageType(id: Option[Long], name: String, masseurId: Long)

  class MassageTypes(tag: Tag) extends RichTable[MassageType](tag, "massage_types") {
    def name = column[String]("name", O.NotNull)
    def masseurId = column[Long]("masseur_id", O.NotNull)

    def masseurFK = foreignKey("masseur_fk", masseurId, TableQuery[Masseurs])(_.id, onUpdate = ForeignKeyAction.Cascade)
    def * = (id.?, name, masseurId) <> (MassageType.tupled, MassageType.unapply)
  }

  case class MassageReservation(id: Option[Long], userId: Long, masseurId: Long, reservationTime: DateTime,
                                starTime: DateTime, endTime: DateTime, paymentDue: Int, comments: String)

  class MassageReservations(tag: Tag) extends RichTable[MassageReservation](tag, "massage_reservations") {

    import java.sql.Timestamp

    implicit def dateTime =
      MappedColumnType.base[DateTime, Timestamp](dt => new Timestamp(dt.getMillis), ts => new DateTime(ts.getTime))

    def userId = column[Long]("user_id")
    def masseurId = column[Long]("masseur_id")
    def reservationTime = column[DateTime]("reservation_time")
    def startTime = column[DateTime]("start_time")
    def endTime = column[DateTime]("end_time")
    def paymentDue = column[Int]("payment_due")
    def comments = column[String]("comments")
    def * = (id.?, userId, masseurId, reservationTime, startTime, endTime, paymentDue, comments) <> (
        MassageReservation.tupled, MassageReservation.unapply)
  }

}
