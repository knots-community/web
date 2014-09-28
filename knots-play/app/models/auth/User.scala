package models.auth

import com.mohiva.play.silhouette.core.exceptions.AuthenticationException
import com.mohiva.play.silhouette.core.{Identity, LoginInfo}
import models.Dao
import models.Models._
import models.RoleType._
import models.db.TableDefinitions._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

import scala.concurrent.Future

/**
 * The user object.
 *
 * @param id The unique ID of the user.
 * @param loginInfo The linked login info.
 * @param firstName Maybe the first name of the authenticated user.
 * @param lastName Maybe the last name of the authenticated user.
 * @param email Maybe the email of the authenticated provider.
 */
case class User(id: Option[Long], loginInfo: LoginInfo, firstName: String, lastName: String,
                email: String, role: Option[List[DbRole]]) extends Identity

object Users extends Dao[models.db.TableDefinitions.Users, DbUser] {

  import models.RoleType

  tableQuery = users

  implicit def user2DbUser(u: User): DbUser = DbUser(u.id, u.firstName, u.lastName, u.email)

  override def getAll = DB withSession { implicit session =>
    users.drop(1) list
  }

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo) = {
    DB withSession { implicit session =>
      Future.successful {

        loginInfos.filter(
          x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey).firstOption match {
          case Some(info) =>
            userLoginInfos.filter(_.loginInfoId === info.id).firstOption match {
              case Some(userLoginInfo) =>
                findById(userLoginInfo.userID) match {
                  case Some(user) =>
                    import models.Roles
                    val roles = Roles.getUserRoles(user.id.get)
                    Some(User(user.id, loginInfo, user.firstName, user.lastName, user.email, Some(roles)))
                  case None => None
                }
              case None => None
            }
          case None => None
        }
      }
    }
  }

  /**
   * Finds a user by its user ID.
   *
   * @param id The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(id: Long) = {
    DB withSession { implicit session =>
      Future.successful {
        users.filter(_.id === id).firstOption match {
          case Some(user) =>
            userLoginInfos.filter(_.userID === user.id).firstOption match {
              case Some(info) =>
                loginInfos.filter(_.id === info.loginInfoId).firstOption match {
                  case Some(loginInfo) =>
                    import models.Roles
                    val roles = Roles.getUserRoles(user.id.get)
                    Some(User(user.id, LoginInfo(loginInfo.providerID, loginInfo.providerKey), user.firstName, user.lastName, user.email, Some(RoleType.fromDbList(roles))))
                  case None => None
                }
              case None => None
            }
          case None => None
        }
      }
    }
  }

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User) = {
    DB withSession { implicit session =>
      Future.successful {
        var userId = Some(0l)
        users.filter(u => u.email === user.email || u.id === user.id).firstOption match {
          case Some(u) =>
            throw new AuthenticationException("User already exists!")
          case None =>
            userId = Some(((users returning users.map(_.id)).insert(DbUser(user.id, user.firstName, user.lastName, user.email))))
            userId
        }


        userRoles += DbUserRole(None, userId, user.role.get(0).id)

        var dbLoginInfo = DbLoginInfo(None, user.loginInfo.providerID, user.loginInfo.providerKey)
        // Insert if it does not exist yet
        loginInfos.filter(
          info => info.providerID === dbLoginInfo.providerID && info.providerKey === dbLoginInfo.providerKey).firstOption match {
          case None => loginInfos.insert(dbLoginInfo)
          case Some(info) => import play.Logger
            Logger.debug("Nothing to insert since info already exists: " + info)
        }
        dbLoginInfo = loginInfos.filter(
          info => info.providerID === dbLoginInfo.providerID && info.providerKey === dbLoginInfo.providerKey).first
        // Now make sure they are connected
        userLoginInfos.filter(
          info => info.userID === userId.get && info.loginInfoId === dbLoginInfo.id).firstOption match {
          case Some(info) =>
          // They are connected already, we could as well omit this case ;)
          case None =>
            userLoginInfos += DbUserLoginInfo(Some(0), userId.get, dbLoginInfo.id.get)
        }

        val newUser = user.copy(id = userId)
        newUser
      }
    }
  }

  def saveWithRole(user: User, rr: List[RoleType]) = DB withSession { implicit session => {
    val userWithId = save(user)
    userWithId match {
      case u: User => {
        val rr212 = rr map (r => DbUserRole(None, u.id, Some(r)))
        rr212.foreach(userRoles.insert(_))
      }
    }
  }
  }

  def initialize = DB withSession { implicit session =>
    if(users.list.length == 0) users += DbUser(None, "", "", "")
  }
}
