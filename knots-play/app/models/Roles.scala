package models

import models.db.Dao
import models.db.TableDefinitions.{Role}
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

import scala.language.implicitConversions

/**
 * Created by anton on 9/19/14.
 */

sealed trait RoleType
case object RegularUserRole extends RoleType
case object AdminRole extends RoleType
case object ProviderRole extends RoleType
case object MasseurRole extends RoleType
case object UnavailableUser extends RoleType

object RoleType {

  implicit def convertRole2RoleType(value: Role) : RoleType =  value match {
    case Role(Some(1), _, _) => RegularUserRole
    case Role(Some(2), _, _) => AdminRole
    case Role(Some(3), _, _) => ProviderRole
    case Role(Some(4), _, _) => MasseurRole
    case Role(Some(5), _, _) => UnavailableUser
  }

  implicit def convertRoleType2Role(value: RoleType) : Role = value match {
    case RegularUserRole => Role(Some(1), "regular", Some(""))
    case AdminRole => Role(Some(2), "admin", Some(""))
    case ProviderRole => Role(Some(3), "provider", Some(""))
    case MasseurRole => Role(Some(4), "masseur", Some(""))
    case UnavailableUser => Role(Some(5), "unavailable", Some("Used to mark unavailable slots for masseurs"))
    case _ => Role(Some(1), "", Some(""))
  }

  implicit def roleType2RoleList(value: RoleType) : List[Role] = List(value)
  implicit def roleType2Long(value: RoleType) : Long = value match {
    case RegularUserRole => 1
    case AdminRole => 2
    case ProviderRole => 3
    case MasseurRole => 4
    case UnavailableUser => 5
  }

  implicit def toDbList(value: List[RoleType]) : List[Role] = { value map {x => convertRoleType2Role(x)}}
  implicit def fromDbList(value: List[Role]) : List[RoleType] = { value map {x => convertRole2RoleType(x)}}
}

object Roles extends Dao {

  //  override var tableQuery = roles
  def getUserRoles(userId: Long) : List[Role] = DB.withSession { implicit session =>
    val x = (for {user <- users
          if (user.id === userId)
          role <- user.roles} yield role).list
    x map (r => roles.filter(_.id === r.roleId) first)
  }

  def isAdmin(userId: Long) = getUserRoles(userId).contains(AdminRole)
//  def isAdmin(u: User) = { u.role map { r => r.contains(AdminRole)} getOrElse (false) }
//  def isProvider(userId: Long) = getUserRoles(userId).contains(ProviderRole)
//  def isProvider(u: User) = { u.role map { r => r.contains(ProviderRole)} getOrElse (false) }
//  def isMasseur(userId: Long) = getUserRoles(userId).contains(MasseurRole)
//  def isMasseur(u: User) = { u.role map { r => r.contains(MasseurRole)} getOrElse (false) }
//
  def initialize() = DB.withSession { implicit session =>
      if(roles.list.length == 0) {
        roles += RegularUserRole
        roles += AdminRole
        roles += ProviderRole
        roles += MasseurRole
        roles += UnavailableUser

      }
  }
}