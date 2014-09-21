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

  tableQuery = roles

  //  override var tableQuery = roles
  def getUserRoles(userId: Option[Long]) = DB.withSession { implicit session =>
    userId map { uid: Long =>
      (for {user <- users
            if (user.id === userId)
            role <- user.roles} yield role).list
    } getOrElse List[DbRole]()
  }

  def isAdmin(userId: Option[Long]) = getUserRoles(userId).contains(AdminRole)
  def isProvider(userId: Option[Long]) = getUserRoles(userId).contains(ProviderRole)
  def isMasseur(userId: Option[Long]) = getUserRoles(userId).contains(MasseurRole)
}