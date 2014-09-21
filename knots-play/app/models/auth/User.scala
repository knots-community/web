package models.auth

import com.mohiva.play.silhouette.core.exceptions.AuthenticationException
import com.mohiva.play.silhouette.core.{Identity, LoginInfo}
import models.Dao
import models.Models._
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
case class User(
  id: Option[Long],
  loginInfo: LoginInfo,
  firstName: Option[String],
  lastName: Option[String],
  email: Option[String],
  role: List[DbRole]
) extends Identity

object Users extends Dao[models.db.TableDefinitions.Users, DbUser] {

  tableQuery = users

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
                    import models.RegularUserRole
//                    val roles: List[DbRole] = Roles.getUserRoles(user.id)
                    Some(User(user.id, loginInfo, user.firstName, user.lastName, user.email, RegularUserRole))
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
                    import models.RegularUserRole
//                    val roles: List[DbRole] = Roles.getUserRoles(user.id)
                    Some(User(user.id, LoginInfo(loginInfo.providerID, loginInfo.providerKey), user.firstName, user.lastName, user.email, RegularUserRole))
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
        var userId = Some(0l)
        users.filter(u => u.email === user.email || u.id === user.id).firstOption match {
          case Some(u) =>
            throw new AuthenticationException("User already exists!")
          case None =>
            userId = Some(((users returning users.map(_.id)).insert(
              DbUser(user.id, user.firstName, user.lastName, user.email))))
            userId
        }


        userRoles += DbUserRole(None, userId, Some(1))

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
        userLoginInfos.filter(info => info.userID === userId.get && info.loginInfoId === dbLoginInfo.id).firstOption match {
          case Some(info) =>
          // They are connected already, we could as well omit this case ;)
          case None =>
            userLoginInfos += DbUserLoginInfo(Some(0), userId.get, dbLoginInfo.id.get)
        }
        Future.successful {user} // We do not change the user => return it
      }

  }
}
