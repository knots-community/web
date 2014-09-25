package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Environment, Silhouette}
import models.{AdminUser, Masseurs}
import models.auth.{User, Users}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by anton on 9/21/14.
 */
class AdminController @Inject()(implicit val env: Environment[AdminUser, CachedCookieAuthenticator])
    extends Silhouette[AdminUser, CachedCookieAuthenticator] {

  /**
   * Handles the Sign Up action.
   *
   * @return The result to display.
   */
  def signup = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => import concurrent.Future
        Future.successful(Redirect(routes.AdminController.index()))
      case None => import forms.SignUpForm
        Future.successful(Ok(views.html.signup(SignUpForm.form)))
    }
  }

  def signin = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Redirect(routes.AdminController.index))
      case None => import forms.SignInForm
        Future.successful(Ok(views.html.admin.signin(SignInForm.form)))
    }
  }

  def index = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Redirect(routes.AdminController.listUsers()))
      case None => import forms.SignInForm
        Future.successful(Ok(views.html.admin.signin(SignInForm.form)))
    }
  }

  def listUsers = UserAwareAction.async { implicit request =>
    val futureUsers = Future {Users.getAll}
    futureUsers.map(users => Ok(views.html.admin.users.list(request.identity, users)))
  }

  def listMasseurs = UserAwareAction.async { implicit request =>
    val futureMasseurs = Future {Masseurs.getAllMasseurs}
    futureMasseurs.map { masseurList => Ok(views.html.admin.masseur.list(request.identity, masseurList))}
  }

  def addMasseur = UserAwareAction.async { implicit request =>
    Future.successful(Ok(views.html.admin.index(request.identity)))
  }

  def calendar = UserAwareAction.async { implicit request =>
    Future.successful(Ok(views.html.admin.index(request.identity)))
  }

}
