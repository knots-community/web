package models.auth

import com.mohiva.play.silhouette.contrib.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.core.LoginInfo
import com.mohiva.play.silhouette.core.providers.PasswordInfo
import models.Dao
import models.Models._
import models.db.TableDefinitions.DbPasswordInfo
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

import scala.concurrent.Future


/**
 * The DAO to store the password information.
 */
class PasswordInfoDao extends DelegableAuthInfoDAO[PasswordInfo] with Dao[models.db.TableDefinitions.PasswordInfos, DbPasswordInfo]  {

  tableQuery = passwordInfos

  /**
   * Saves the password info.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The password info to save.
   * @return The saved password info or None if the password info couldn't be saved.
   */
  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    Future.successful {
      DB withSession {implicit session =>
        val infoId = loginInfos.filter(
          x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey
        ).first.id.get
        passwordInfos insert DbPasswordInfo(None, authInfo.hasher, authInfo.password, authInfo.salt, infoId)
        authInfo
      }
    }
  }

  /**
   * Finds the password info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved password info or None if no password info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    Future.successful {
      DB withSession { implicit session =>
        loginInfos.filter(info => info.providerID === loginInfo.providerID && info.providerKey === loginInfo.providerKey).firstOption match {
          case Some(info) =>
            val passwordInfo = passwordInfos.filter(_.loginInfoId === info.id).first
            Some(PasswordInfo(passwordInfo.hasher, passwordInfo.password, passwordInfo.salt))
          case None => None
        }
      }
    }
  }
}
