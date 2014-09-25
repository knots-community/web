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
case object UnavailableUser extends RoleType

object RoleType {

  implicit def convertDbRole2Role(value: DbRole) : RoleType =  value match {
    case DbRole(Some(1), _, _) => RegularUserRole
    case DbRole(Some(2), _, _) => AdminRole
    case DbRole(Some(3), _, _) => ProviderRole
    case DbRole(Some(4), _, _) => MasseurRole
    case DbRole(Some(5), _, _) => UnavailableUser
  }

  implicit def convertRole2DbRole(value: RoleType) : DbRole = value match {
    case RegularUserRole => DbRole(Some(1), "regular", Some(""))
    case AdminRole => DbRole(Some(2), "admin", Some(""))
    case ProviderRole => DbRole(Some(3), "provider", Some(""))
    case MasseurRole => DbRole(Some(4), "masseur", Some(""))
    case UnavailableUser => DbRole(Some(5), "unavailable", Some("Used to mark unavailable slots for masseurs"))
    case _ => DbRole(Some(1), "", Some(""))
  }

  implicit def role2List(value: RoleType) : List[DbRole] = List(value)
  implicit def role2Long(value: RoleType) : Long = value match {
    case RegularUserRole => 1
    case AdminRole => 2
    case ProviderRole => 3
    case MasseurRole => 4
    case UnavailableUser => 5
  }

  implicit def toDbList(value: List[RoleType]) : List[DbRole] = { value map {x => convertRole2DbRole(x)}}
  implicit def fromDbList(value: List[DbRole]) : List[RoleType] = { value map {x => convertDbRole2Role(x)}}
}