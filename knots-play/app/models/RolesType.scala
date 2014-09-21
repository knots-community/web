package models
import db.TableDefinitions.DbRole

/**
 * Created by anton on 9/20/14.
 */

sealed trait RoleType
case object RegularUserRole extends RoleType
case object AdminRole extends RoleType
case object ProviderRole extends RoleType
case object MasseurRole extends RoleType

object RoleType {

  implicit def convertDbRole2Role(value: DbRole) : RoleType =  value match {
    case DbRole(Some(0), _, _) => RegularUserRole
    case DbRole(Some(1), _, _) => AdminRole
    case DbRole(Some(2), _, _) => ProviderRole
    case DbRole(Some(3), _, _) => MasseurRole
  }

  implicit def convertRole2DbRole(value: RoleType) : DbRole = value match {
    case RegularUserRole => DbRole(Some(0), "regular", Some(""))
    case AdminRole => DbRole(Some(1), "admin", Some(""))
    case ProviderRole => DbRole(Some(2), "provider", Some(""))
    case MasseurRole => DbRole(Some(3), "masseur", Some(""))
    case _ => DbRole(Some(3), "", Some(""))
  }

  implicit def role2List(value: RoleType) : List[DbRole] = List(value)

}