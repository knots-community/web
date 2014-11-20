package models

import com.mohiva.play.silhouette.core.exceptions.AuthenticationException
import com.mohiva.play.silhouette.core.{Identity, LoginInfo => SilhouetteLoginInfo}
import models.db.AuthTableDefinitions.{LoginInfo, UserLoginInfo}
import models.db.Dao
import models.db.TableDefinitions.{UserRole, Admin, User}
import play.Logger
import play.api.Play.current
import models.implicits.AdminUserConversions._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

import scala.concurrent.Future

/*
 * Created by anton on 9/21/14.
 */
case class AdminUser(id: Option[Long], loginInfo: SilhouetteLoginInfo, firstName: String, lastName: String,
                     email: String, userId: Option[Long]) extends Identity
object AdminUsers extends Dao {

  implicit def admin2AdminUser(admin: AdminUser): Admin = {
    return Admin(admin.id, admin.userId)
  }

  def save(admin: AdminUser) = DB.withSession { implicit session =>
    Future.successful {
      
      var newUserId = Some(0l)
      users.filter(u => u.email === admin.email || u.id === admin.id).firstOption match {
        case Some(u) =>
          throw new AuthenticationException("User already exists!")
        case None =>
          newUserId = Some(((users returning users.map(_.id)).insert(User(admin.id, admin.firstName, admin.lastName, admin.email, 1))))
          newUserId
      }

      userRoles += UserRole(None, newUserId, Some(AdminRole))

      var dbLoginInfo = LoginInfo(None, admin.loginInfo.providerID, admin.loginInfo.providerKey)
      // Insert if it does not exist yet
      loginInfos.filter(
        info => info.providerId === dbLoginInfo.providerId && info.providerKey === dbLoginInfo.providerKey).firstOption match {
        case None => loginInfos.insert(dbLoginInfo)
        case Some(info) => Logger.debug("Nothing to insert since info already exists: " + info)
      }
      dbLoginInfo = loginInfos.filter(
        info => info.providerId === dbLoginInfo.providerId && info.providerKey === dbLoginInfo.providerKey).first
      // Now make sure they are connected
      userLoginInfos.filter(
        info => info.userId === newUserId.get && info.loginInfoId === dbLoginInfo.id).firstOption match {
        case Some(info) =>
        // They are connected already, we could as well omit this case ;)
        case None =>
          userLoginInfos += UserLoginInfo(Some(0), newUserId.get, dbLoginInfo.id.get)
      }

      val newAdmin = admin.copy(userId = newUserId)
      admins += newAdmin
      newAdmin
    }
  }

  def find(loginInfo: SilhouetteLoginInfo) = DB.withSession { implicit session =>
    Future.successful {
      loginInfos.filter(
        x => x.providerId === loginInfo.providerID && x.providerKey === loginInfo.providerKey).firstOption match {
        case Some(info) =>
          userLoginInfos.filter(_.loginInfoId === info.id).firstOption match {
            case Some(userLoginInfo) =>
              Users.findById(userLoginInfo.userId) match {
                case Some(user) =>
                  admins.filter(_.userId === user.id).firstOption match {
                    case Some(admin) => Some(AdminUser(admin.id, loginInfo, user.firstName, user.lastName, user.email, user.id))
                    case None => None
                  }
                case None => None
              }
            case None => None
          }
        case None => None
      }
    }
  }

}