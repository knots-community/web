package models.db

import models.db.AuthTableDefinitions._
import models.db.TableDefinitions._
import play.api.db.slick.Config.driver.simple._

/**
 * Created by anton on 9/18/14.
 */
trait Dao {

  val users = TableQuery[Users]
  val roles = TableQuery[Roles]
  val userRoles = TableQuery[UserRoles]
  val loginInfos = TableQuery[LoginInfos]
  val userLoginInfos = TableQuery[UserLoginInfos]
  val passwordInfos = TableQuery[PasswordInfos]
  val massageTypes = TableQuery[MassageTypes]
  val masseurs = TableQuery[Masseurs]
  val reservations = TableQuery[MassageReservations]
  val admins = TableQuery[Admins]
  val reservationTypes = TableQuery[ReservationTypes]
  val masseurMassageTypes = TableQuery[MasseurMassageTypes]
  val tokenPasswords = TableQuery[TokenPasswords]
  val timeSlots = TableQuery[TimeSlots]
  val companies = TableQuery[Companies]
  val events = TableQuery[Events]
}