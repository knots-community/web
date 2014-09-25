package models

import db.TableDefinitions.DbRole
import models.Models._
import play.api.Play.current
import play.api.db.slick._
import play.api.db.slick.Config.driver.simple._
import scala.language.implicitConversions

/**
 * Created by anton on 9/19/14.
 */
object Roles extends Dao[models.db.TableDefinitions.Roles, DbRole] {

  import auth.User

  tableQuery = roles

  //  override var tableQuery = roles
  def getUserRoles(userId: Long) : List[DbRole] = DB.withSession { implicit session =>
    val x = (for {user <- users
          if (user.id === userId)
          role <- user.roles} yield role).list
    x map (r => roles.filter(_.id === r.roleId) first)
  }

  def isAdmin(userId: Long) = getUserRoles(userId).contains(AdminRole)
  def isAdmin(u: User) = { u.role map { r => r.contains(AdminRole)} getOrElse (false) }
  def isProvider(userId: Long) = getUserRoles(userId).contains(ProviderRole)
  def isProvider(u: User) = { u.role map { r => r.contains(ProviderRole)} getOrElse (false) }
  def isMasseur(userId: Long) = getUserRoles(userId).contains(MasseurRole)
  def isMasseur(u: User) = { u.role map { r => r.contains(MasseurRole)} getOrElse (false) }

  def initialize() = DB.withSession { implicit session =>
      if(roles.list.length ==0) {
        roles += RegularUserRole
        roles += AdminRole
        roles += ProviderRole
        roles += MasseurRole
        roles += UnavailableUser

      }
  }
}