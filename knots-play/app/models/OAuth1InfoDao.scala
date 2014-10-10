package models

import com.mohiva.play.silhouette.contrib.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.core.LoginInfo
import com.mohiva.play.silhouette.core.providers.OAuth1Info
import models.db.TableDefinitions.DbOAuth1Info
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

import scala.concurrent.Future

/**
 * The DAO to store the OAuth1 information.
 */
class OAuth1InfoDao extends DelegableAuthInfoDAO[OAuth1Info] with Dao {

  /**
   * Saves the OAuth1 info.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The OAuth1 info to save.
   * @return The saved OAuth1 info or None if the OAuth1 info couldn't be saved.
   */
  def save(loginInfo: LoginInfo, authInfo: OAuth1Info): Future[OAuth1Info] = {
    Future.successful(
      DB withSession { implicit session =>
        val infoId = loginInfos.filter(
          x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey
        ).first.id.get
        oAuth1Infos.filter(_.loginInfoId === infoId).firstOption match {
          case Some(info) =>
            oAuth1Infos update DbOAuth1Info(info.id, authInfo.token, authInfo.secret, infoId)
          case None =>
            oAuth1Infos insert DbOAuth1Info(None, authInfo.token, authInfo.secret, infoId)
        }
        authInfo
      }
    )
  }

  /**
   * Finds the OAuth1 info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved OAuth1 info or None if no OAuth1 info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[OAuth1Info]] = {
    Future.successful(
      DB withSession { implicit session =>
        loginInfos.filter(info => info.providerID === loginInfo.providerID && info.providerKey === loginInfo.providerKey).firstOption match {
          case Some(info) =>
            val oAuth1Info = oAuth1Infos.filter(_.loginInfoId === info.id).first
            Some(OAuth1Info(oAuth1Info.token, oAuth1Info.secret))
          case None => None
        }
      }
    )
  }
}
