package models.auth

import com.mohiva.play.silhouette.contrib.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.core.LoginInfo
import com.mohiva.play.silhouette.core.providers.OAuth2Info
import models.Dao
import models.Models._
import models.db.TableDefinitions.DbOAuth2Info
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

import scala.concurrent.Future

/**
 * The DAO to store the OAuth2 information.
 */
class OAuth2InfoDao extends DelegableAuthInfoDAO[OAuth2Info] with Dao[models.db.TableDefinitions.OAuth2Infos, DbOAuth2Info]  {

  tableQuery = oAuth2Infos

  /**
   * Saves the OAuth2 info.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The OAuth2 info to save.
   * @return The saved OAuth2 info or None if the OAuth2 info couldn't be saved.
   */
  def save(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = {
    Future.successful(
      DB withSession { implicit session =>
        val infoId = loginInfos.filter(
          x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey
        ).first.id.get
        oAuth2Infos.filter(_.loginInfoId === infoId).firstOption match {
          case Some(info) =>
            oAuth2Infos update DbOAuth2Info(info.id, authInfo.accessToken, authInfo.tokenType, authInfo.expiresIn, authInfo.refreshToken, infoId)
          case None => oAuth2Infos insert DbOAuth2Info(None, authInfo.accessToken, authInfo.tokenType, authInfo.expiresIn, authInfo.refreshToken, infoId)
        }
        authInfo
      }
    )
  }

  /**
   * Finds the OAuth2 info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved OAuth2 info or None if no OAuth2 info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[OAuth2Info]] = {
    Future.successful(
      DB withSession { implicit session =>
        loginInfos.filter(info => info.providerID === loginInfo.providerID && info.providerKey === loginInfo.providerKey).firstOption match {
          case Some(info) =>
            val oAuth2Info = oAuth2Infos.filter(_.loginInfoId === info.id).first
            Some(OAuth2Info(oAuth2Info.accessToken, oAuth2Info.tokenType, oAuth2Info.expiresIn, oAuth2Info.refreshToken))
          case None => None
        }
      }
    )
  }
}
