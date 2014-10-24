package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Silhouette, Environment}
import models.{CompaniesDao, AdminUser}

/**
 * Created by anton on 10/23/14.
 */
class CompaniesController @Inject()(implicit val env: Environment[AdminUser, CachedCookieAuthenticator])
  extends Silhouette[AdminUser, CachedCookieAuthenticator] {

  def list = SecuredAction { implicit request =>
    val companis = CompaniesDao.findAll
    Ok(views.html.admin.companies.list(Some(request.identity), companis))}
  }

def find(id: Long) = SecuredAction {
implicit request =>

}

def save = SecuredAction {
implicit request =>
}

def update(id: Long) = SecuredAction {
implicit request =>

}
}

