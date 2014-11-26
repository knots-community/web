package models.db

import java.sql.Timestamp

import org.joda.time.{DateTime}
import org.joda.time.DateTimeZone._
import play.api.db.slick.Config.driver.simple._
import utils.db.RichTable
import com.github.tototoshi.slick.PostgresJodaSupport
import utils.db.DateTimeConversion._

object TableDefinitions {

  case class Role(id: Option[Long], name: String, description: Option[String])
  class Roles(tag: Tag) extends RichTable[Role](tag, "role") {
    def name = column[String]("name", O.NotNull)
    def description = column[Option[String]]("description", O.Nullable)

    def roles_idx = index("roles_idx_id_name", (id, name), unique = true)

    def * = (id.?, name, description) <>(Role.tupled, Role.unapply)
    def users = TableQuery[UserRoles].filter(_.roleId === id).flatMap(_.userFK)
  }

  case class User(id: Option[Long], firstName: String, lastName: String, email: String, companyId: Long)
  class Users(tag: Tag) extends RichTable[User](tag, "user") {
    def firstName = column[String]("first_name", O.NotNull)
    def lastName = column[String]("last_name", O.NotNull)
    def email = column[String]("email", O.NotNull)
    def companyId = column[Long]("company", O.NotNull)

    def companyFk = foreignKey("company_fk", companyId, TableQuery[Companies])(_.id, onUpdate = ForeignKeyAction.Cascade)

    def * = (id.?, firstName, lastName, email, companyId) <> (User.tupled, User.unapply)
    def roles = TableQuery[UserRoles].filter(_.userId === id)
  }

  case class Admin(id: Option[Long], userId: Option[Long])
  class Admins(tag: Tag) extends RichTable[Admin](tag, "admin") {
    def userId = column[Long]("user_id", O.NotNull)

    def userFk = foreignKey("user_fk", userId, TableQuery[Users])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

    def * = (id.?, userId.?) <>(Admin.tupled, Admin.unapply)
  }

  case class UserRole(id: Option[Long], userId: Option[Long], roleId: Option[Long])
  class UserRoles(tag: Tag) extends RichTable[UserRole](tag, "user_role") {
    def userId = column[Long]("user_id")
    def roleId = column[Long]("role_id")

    def userFK = foreignKey("user_fk", userId, TableQuery[Users])(_.id , onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
    def roleFK = foreignKey("role_fk", roleId, TableQuery[Roles])(_.id , onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (id.?, userId.?, roleId.?) <>(UserRole.tupled, UserRole.unapply)
  }

  case class Company(id: Option[Long], name: String, address: String, phone: String, contactEmail: String, signupLink: Option[String])
  class Companies(tag: Tag) extends RichTable[Company](tag, "company") {
    def name = column[String]("name", O.NotNull)
    def address = column[String]("address", O.NotNull)
    def phone = column[String]("phone", O.NotNull)
    def contactEmail = column[String]("email", O.NotNull)
    def signupLink = column[String]("signup_link", O.NotNull)

    def * = (id.?, name, address, phone, contactEmail, signupLink.?) <> (Company.tupled, Company.unapply)
  }

  case class Masseur(id: Option[Long], userId: Long, sex: String, isActive: Boolean)
  class Masseurs(tag: Tag) extends RichTable[Masseur](tag, "masseur") {
    def userId = column[Long]("user_id", O.NotNull)
    def sex = column[String]("sex", O.NotNull)
    def isActive = column[Boolean]("is_active", O.NotNull)

    def userFk = foreignKey("user_fk", userId, TableQuery[Users])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

    def * = (id.?, userId, sex, isActive) <> (Masseur.tupled, Masseur.unapply)
  }

  case class MasseurMassageType(id: Option[Long], masseurId: Long, massageTypeId: Long)
  class MasseurMassageTypes(tag: Tag) extends RichTable[MasseurMassageType](tag, "masseur_massage_type") {
    def masseurId = column[Long]("masseur_id", O.NotNull)
    def massageTypeId = column[Long]("massage_type_id", O.NotNull)

    def masseurFk = foreignKey("masseur_fk", masseurId, TableQuery[Masseurs])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
    def massageTypeFk = foreignKey("massage_type_fk", massageTypeId, TableQuery[MassageTypes])(_.id, onUpdate = ForeignKeyAction.Cascade)

    def * = (id.?, masseurId, massageTypeId) <> (MasseurMassageType.tupled, MasseurMassageType.unapply)
  }

  case class MassageType(id: Option[Long], name: String, description: Option[String])
  class MassageTypes(tag: Tag) extends RichTable[MassageType](tag, "massage_type") {
    def name = column[String]("name", O.NotNull)
    def description = column[Option[String]]("masseur_id", O.Nullable)

    def * = (id.?, name, description) <> (MassageType.tupled, MassageType.unapply)
  }

  case class TimeSlot(id: Option[Long], masseurId: Long, startTime: DateTime, endTime: DateTime, status: Int, eventId: Long)
  case class TimeSlots(tag: Tag) extends RichTable[TimeSlot](tag, "time_slot") {
    def masseurId = column[Long]("masseur_id", O.NotNull)
    def startTime = column[DateTime]("start_time", O.NotNull)
    def endTime = column[DateTime]("start_time", O.NotNull)
    def status = column[Int]("status", O.NotNull)
    def eventId = column[Long]("event_id", O.NotNull)

    def masseurFk = foreignKey("masseur_fk", masseurId, TableQuery[Masseurs])(_.id, onUpdate = ForeignKeyAction.Cascade)
    def eventFk = foreignKey("event_fk", eventId, TableQuery[Events])(_.id, onUpdate = ForeignKeyAction.Cascade)
    def idx = index("time_slot_idx_masser_id_start_time_event_id", (masseurId, startTime, eventId), unique = true)

    def * = (id.?, masseurId, startTime, endTime, status, eventId) <> (TimeSlot.tupled, TimeSlot.unapply)
  }

  case class Event(id: Option[Long], date: DateTime, start: DateTime, end: DateTime, companyId: Long, eventType: String)
  case class Events(tag: Tag) extends RichTable[Event](tag, "event") {
    def date = column[DateTime]("event_date", O.NotNull)
    def start = column[DateTime]("start", O.NotNull)
    def end = column[DateTime]("end", O.NotNull)
    def companyId = column[Long]("company_id", O.NotNull)
    def eventType = column[String]("event_type", O.NotNull)

    def companyFk = foreignKey("company_fk", companyId, TableQuery[Companies])(_.id, onUpdate = ForeignKeyAction.Cascade)

    def * = (id.?, date, start, end, companyId, eventType) <> (Event.tupled, Event.unapply)
  }

  case class ReservationType(id: Option[Long], name: String, description: Option[String])
  class ReservationTypes(tag: Tag) extends RichTable[ReservationType](tag, "reservation_type") {
    def name = column[String]("name", O.NotNull)
    def description = column[Option[String]]("description", O.Nullable)

    def * = (id.?, name, description) <> (ReservationType.tupled, ReservationType.unapply)
  }

  case class MassageReservation(id: Option[Long], userId: Long, masseurId: Long, reservationTime: DateTime,
                                paymentDue: Int, comments: Option[String],
                                reservationType: Long, massageType: Long, timeSlotId: Long)

  class MassageReservations(tag: Tag) extends RichTable[MassageReservation](tag, "massage_reservation") {
    def userId = column[Long]("user_id", O.NotNull)
    def masseurId = column[Long]("masseur_id", O.NotNull)
    def reservationTime = column[DateTime]("reservation_time", O.NotNull)
    def paymentDue = column[Int]("payment_due", O.NotNull)
    def comments = column[String]("comments", O.Nullable)
    def typeId = column[Long]("reservation_type_id", O.NotNull)
    def massageTypeId = column[Long]("massage_type_id", O.NotNull)
    def timeSlotId = column[Long]("time_slot_id", O.NotNull)

    def idx = index("reservation_idx_time_slot_id_user_id", (timeSlotId, userId), unique = true)
    def userFk = foreignKey("user_fk", userId, TableQuery[Users])(_.id, onUpdate = ForeignKeyAction.Cascade)
    def masseurFk = foreignKey("masseur_fk", masseurId, TableQuery[Masseurs])(_.id, onUpdate = ForeignKeyAction.Cascade)
    def typeFk = foreignKey("type_fk", typeId, TableQuery[ReservationTypes])(_.id, onUpdate = ForeignKeyAction.Cascade)
    def massageTypeFk = foreignKey("massage_type_fk", massageTypeId, TableQuery[MassageTypes])(_.id, onUpdate = ForeignKeyAction.Cascade)
    def timeSlotFk = foreignKey("time_slot_fk", timeSlotId, TableQuery[TimeSlots])(_.id, onUpdate = ForeignKeyAction.Cascade)

    def * = (id.?, userId, masseurId, reservationTime, paymentDue, comments.?, typeId, massageTypeId, timeSlotId) <>
      (MassageReservation.tupled, MassageReservation.unapply)
  }
}
