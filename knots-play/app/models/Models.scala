package models

import play.api.db.slick.Config.driver.simple._
import db.TableDefinitions._

/**
 * Created by anton on 9/19/14.
 */
object Models {

  val users = TableQuery[Users]
  val roles = TableQuery[Roles]
  val userRoles = TableQuery[UserRoles]
  val loginInfos = TableQuery[LoginInfos]
  val userLoginInfos = TableQuery[UserLoginInfos]
  val passwordInfos = TableQuery[PasswordInfos]
  val oAuth1Infos = TableQuery[OAuth1Infos]
  val oAuth2Infos = TableQuery[OAuth2Infos]
  val massageTypes = TableQuery[MassageTypes]
  val masseurs = TableQuery[Masseurs]
  val reservations = TableQuery[MassageReservations]
  val admins = TableQuery[Admins]
  val reservationTypes = TableQuery[ReservationTypes]
  val masseurMassageTypes = TableQuery[MasseurMassageTypes]
  val pastReservations = TableQuery[PastReservations]
}