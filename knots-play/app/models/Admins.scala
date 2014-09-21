package models

import auth.User
import com.mohiva.play.silhouette.core.LoginInfo
import db.TableDefinitions.{DbRole, Admin}
import Models._
import db.TableDefinitions.DbUser
import auth.Users

/**
 * Created by anton on 9/21/14.
 */
case class AdminUser(override val id: Option[Long], override val loginInfo: LoginInfo,
                     override val firstName: Option[String], override val lastName: Option[String],
                     override val email: Option[String], override val role: List[DbRole],
                     val userId: Option[Long]) extends User(id, loginInfo, firstName, lastName, email, role) {

  implicit def admin2DbUser(admin: AdminUser): DbUser = {
    return DbUser(admin.id, admin.firstName, admin.lastName, admin.email)
  }

  implicit def admin2AdminUser(admin: AdminUser): Admin = {
    return Admin(admin.id, admin.userId)
  }

}

object Admins extends Dao[db.TableDefinitions.Admins, Admin] {
  tableQuery = admins

  def insert(admin: AdminUser) = {
    val id = Users.insert(DbUser(admin.id, admin.firstName, admin.lastName, admin.email))
    id match {
      case i: Option[Long] => {
        val a2 = AdminUser(admin.id, admin.loginInfo, admin.firstName, admin.lastName, admin.email, AdminRole, i)
        super.insert(Admin(a2.id, a2.userId))
      }
      case _ => 0
    }
  }
}