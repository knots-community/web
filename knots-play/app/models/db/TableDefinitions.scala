package models.db

import java.sql.Timestamp

import org.joda.time.{LocalTime, DateTime}
import org.joda.time.DateTimeZone._
import play.api.db.slick.Config.driver.simple._
import utils.db.RichTable
import com.github.tototoshi.slick.PostgresJodaSupport

object TableDefinitions {

  implicit val dateType = MappedColumnType.base[DateTime, Timestamp](
  {d => new Timestamp(d.getMillis)} ,
  {d => new DateTime(d.getTime, UTC)}
  )

  case class DbRole(id: Option[Long], name: String, description: Option[String])

  class Roles(tag: Tag) extends RichTable[DbRole](tag, "role") {
    def name = column[String]("name", O.NotNull)
    def description = column[Option[String]]("description", O.Nullable)

    def * = (id.?, name, description) <>(DbRole.tupled, DbRole.unapply)
    def roles_idx = index("roles_idx", (id, name), unique = true)
    def users = TableQuery[UserRoles].filter(_.roleId === id).flatMap(_.userFK)
  }

  case class User(id: Option[Long], firstName: String, lastName: String, email: String, companyId: Long)

  class Users(tag: Tag) extends RichTable[User](tag, "users") {
    def firstName = column[String]("firstName", O.NotNull)
    def lastName = column[String]("lastName", O.NotNull)
    def email = column[String]("email", O.NotNull)
    def companyId = column[Long]("company", O.NotNull)
    def * = (id.?, firstName, lastName, email, companyId) <> (User.tupled, User.unapply)
    def roles = TableQuery[UserRoles].filter(_.userId === id)

    def companyFk = foreignKey("companyFk", companyId, TableQuery[Companies])(_.id, onUpdate = ForeignKeyAction.Cascade)
  }

  case class Admin(id: Option[Long], userId: Option[Long])

  class Admins(tag: Tag) extends RichTable[Admin](tag, "admins") {
    def userId = column[Long]("userId", O.NotNull)
    def userFk = foreignKey("user_fk", userId, TableQuery[Users])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
    def * = (id.?, userId.?) <>(Admin.tupled, Admin.unapply)
  }

  case class DbUserRole(id: Option[Long], userId: Option[Long], roleId: Option[Long])

  class UserRoles(tag: Tag) extends RichTable[DbUserRole](tag, "user_role") {
    def userId = column[Long]("userId")
    def roleId = column[Long]("roleId")

    def userFK = foreignKey("user_fk", userId, TableQuery[Users])(_.id , onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
    def roleFK = foreignKey("role_fk", roleId, TableQuery[Roles])(_.id , onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (id.?, userId.?, roleId.?) <>(DbUserRole.tupled, DbUserRole.unapply)
  }

  case class Company(id: Option[Long], name: String, address: String, phone: String, contactEmail: String, signupLink: Option[String])
  class Companies(tag: Tag) extends RichTable[Company](tag, "companies") {
    def name = column[String]("name")
    def address = column[String]("address")
    def phone = column[String]("phone")
    def contactEmail = column[String]("email")
    def signupLink = column[String]("signupLink")

    def * = (id.?, name, address, phone, contactEmail, signupLink.?) <> (Company.tupled, Company.unapply)
  }

  case class DbLoginInfo(id: Option[Long], providerID: String, providerKey: String)

  class LoginInfos(tag: Tag) extends RichTable[DbLoginInfo](tag, "logininfo") {
    def providerID = column[String]("providerID")
    def providerKey = column[String]("providerKey")
    def * = (id.?, providerID, providerKey) <>(DbLoginInfo.tupled, DbLoginInfo.unapply)
  }

  case class DbUserLoginInfo(id: Option[Long], userID: Long, loginInfoId: Long)

  class UserLoginInfos(tag: Tag) extends RichTable[DbUserLoginInfo](tag, "userlogininfo") {
    def userID = column[Long]("userID", O.NotNull)
    def loginInfoId = column[Long]("loginInfoId", O.NotNull)
    def loginInfoFk = foreignKey("login_info_fk", loginInfoId, TableQuery[LoginInfos])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
    def * = (id.?, userID, loginInfoId) <>(DbUserLoginInfo.tupled, DbUserLoginInfo.unapply)
  }

  case class DbPasswordInfo(id: Option[Long], hasher: String, password: String, salt: Option[String], loginInfoId: Long)

  case class TokenPassword(id: Option[Long], token: String, userId: Long)
  class TokenPasswords(tag: Tag) extends RichTable[TokenPassword](tag, "tokenpassword") {
    def token = column[String]("token")
    def userId = column[Long]("userId")
    def userFk = foreignKey("user_fk", userId, TableQuery[Users])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

    def * = (id.?, token, userId) <> (TokenPassword.tupled, TokenPassword.unapply)
  }

  class PasswordInfos(tag: Tag) extends RichTable[DbPasswordInfo](tag, "passwordinfo") {
    def hasher = column[String]("hasher")
    def password = column[String]("password")
    def salt = column[Option[String]]("salt")
    def loginInfoId = column[Long]("loginInfoId")
    def loginInfoFk = foreignKey("login_info_fk", loginInfoId, TableQuery[LoginInfos])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
    def * = (id.?, hasher, password, salt, loginInfoId) <>(DbPasswordInfo.tupled, DbPasswordInfo.unapply)
  }

  case class DbOAuth1Info(id: Option[Long], token: String, secret: String, loginInfoId: Long)

  class OAuth1Infos(tag: Tag) extends RichTable[DbOAuth1Info](tag, "oauth1info") {
    def token = column[String]("token")
    def secret = column[String]("secret")
    def loginInfoId = column[Long]("loginInfoId")
    def loginInfoFk = foreignKey("login_info_fk", loginInfoId, TableQuery[LoginInfos])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
    def * = (id.?, token, secret, loginInfoId) <>(DbOAuth1Info.tupled, DbOAuth1Info.unapply)
  }

  case class DbOAuth2Info(id: Option[Long], accessToken: String, tokenType: Option[String], expiresIn: Option[Int],
                          refreshToken: Option[String], loginInfoId: Long)

  class OAuth2Infos(tag: Tag) extends RichTable[DbOAuth2Info](tag, "oauth2info") {
    def accessToken = column[String]("accesstoken")
    def tokenType = column[Option[String]]("tokentype")
    def expiresIn = column[Option[Int]]("expiresin")
    def refreshToken = column[Option[String]]("refreshtoken")
    def loginInfoId = column[Long]("logininfoid")
    def loginInfoFk = foreignKey("login_info_fk", loginInfoId, TableQuery[LoginInfos])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
    def * = (id.?, accessToken, tokenType, expiresIn, refreshToken, loginInfoId) <>(DbOAuth2Info.tupled, DbOAuth2Info.unapply)
  }

  case class Masseur(id: Option[Long], userId: Long, sex: String, isActive: Boolean)

  class Masseurs(tag: Tag) extends RichTable[Masseur](tag, "masseurs") {
    def userId = column[Long]("userId", O.NotNull)
    def sex = column[String]("sex", O.NotNull)
    def isActive = column[Boolean]("isActive", O.NotNull)
    def userFk = foreignKey("user_fk", userId, TableQuery[Users])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

    def * = (id.?, userId, sex, isActive) <>(Masseur.tupled, Masseur.unapply)
  }

  case class MasseurMassageType(id: Option[Long], masseurId: Long, massageTypeId: Long)

  class MasseurMassageTypes(tag: Tag) extends RichTable[MasseurMassageType](tag, "masseur_massage_type") {
    def masseurId = column[Long]("masseur_id", O.NotNull)
    def massageTypeId = column[Long]("massage_type_id", O.NotNull)
    def * = (id.?, masseurId, massageTypeId) <>(MasseurMassageType.tupled, MasseurMassageType.unapply)
    def masseurFk = foreignKey("masseur_fk", masseurId, TableQuery[Masseurs])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
    def massageTypeFk = foreignKey("massage_type_fk", massageTypeId, TableQuery[MassageTypes])(_.id, onUpdate = ForeignKeyAction.Cascade)

  }

  case class MassageType(id: Option[Long], name: String, description: Option[String])

  class MassageTypes(tag: Tag) extends RichTable[MassageType](tag, "massage_types") {
    def name = column[String]("name", O.NotNull)
    def description = column[String]("masseurId")

    def * = (id.?, name, description.?) <>(MassageType.tupled, MassageType.unapply)
  }

  case class TimeSlot(id: Option[Long], masseurId: Long, startTime: DateTime, status: Int, companyId: Long)

  case class TimeSlots(tag: Tag) extends RichTable[TimeSlot](tag, "time_slots") {
    def masseurId = column[Long]("masseurId", O.NotNull)
    def startTime = column[DateTime]("startTime", O.NotNull)
    def status = column[Int]("status")
    def companyId = column[Long]("companyId", O.NotNull)

    def masseurFk = foreignKey("masseurFk", masseurId, TableQuery[Masseurs])(_.id, onUpdate = ForeignKeyAction.Cascade)
    def companyFk = foreignKey("companyFk", companyId, TableQuery[Companies])(_.id, onUpdate = ForeignKeyAction.Cascade)
    def idx = index("idx", (masseurId, startTime, companyId), unique = true)
    def * = (id.?, masseurId, startTime, status, companyId) <> (TimeSlot.tupled, TimeSlot.unapply)
  }

  case class ReservationType(id: Option[Long], name: String, description: Option[String])

  class ReservationTypes(tag: Tag) extends RichTable[ReservationType](tag, "reservation_types") {
    def name = column[String]("name", O.NotNull)
    def description = column[String]("description")

    def * = (id.?, name, description.?) <>(ReservationType.tupled, ReservationType.unapply)
  }

  case class MassageReservation(id: Option[Long], userId: Long, masseurId: Long, reservationTime: DateTime,
                                slotId: Long, paymentDue: Int, comments: Option[String],
                                reservationType: Long, massageType: Long, timeSlotId: Long)

  class MassageReservations(tag: Tag) extends RichTable[MassageReservation](tag, "massage_reservations") {
    def userId = column[Long]("user_id", O.NotNull)
    def masseurId = column[Long]("masseur_id", O.NotNull)
    def reservationTime = column[DateTime]("reservation_time", O.NotNull)
    def paymentDue = column[Int]("payment_due", O.NotNull)
    def comments = column[String]("comments")
    def typeId = column[Long]("reservation_type_id", O.NotNull)
    def massageTypeId = column[Long]("massage_type_id", O.NotNull)
    def timeSlotId = column[Long]("time_slot_id", O.NotNull)
    def * = (id.?, userId, masseurId, reservationTime, timeSlotId, paymentDue, comments.?, typeId, massageTypeId, timeSlotId) <>(
        MassageReservation.tupled, MassageReservation.unapply)
    def res_idx = index("res_idx", (timeSlotId, userId), unique = true)
    def userFk = foreignKey("user_fk", userId, TableQuery[Users])(_.id, onUpdate = ForeignKeyAction.Cascade)
    def masseurFk = foreignKey("masseur_fk", masseurId, TableQuery[Masseurs])(_.id, onUpdate = ForeignKeyAction.Cascade)
    def typeFk = foreignKey("type_fk", typeId, TableQuery[ReservationTypes])(_.id, onUpdate = ForeignKeyAction.Cascade)
    def massageTypeFk = foreignKey("massage_type_fk", massageTypeId, TableQuery[MassageTypes])(_.id, onUpdate = ForeignKeyAction.Cascade)
    def timeSlotFk = foreignKey("time_slot_fk", timeSlotId, TableQuery[TimeSlots])(_.id, onUpdate = ForeignKeyAction.Cascade)
  }
}
