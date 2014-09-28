package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Environment, Silhouette}
import forms._
import models.auth.User

import scala.concurrent.Future

/**
 * The basic application controller.
 *
 * @param env The Silhouette environment.
 */
class ApplicationController @Inject() (implicit val env: Environment[User, CachedCookieAuthenticator])
  extends Silhouette[User, CachedCookieAuthenticator] {

  /**
   * Handles the index action.
   *
   * @return The result to display.
   */
  def index = UserAwareAction { implicit request =>
      Ok(views.html.index(request.identity))
  }

  /**
   * Handles the Sign In action.
   *
   * @return The result to display.
   */
  def signin = UserAwareAction.async { implicit request =>
    request.identity match {
//      case Some(user) => Future.successful(Redirect(routes.ApplicationController.index))
      case None => Future.successful(Ok(views.html.signin(SignInForm.form)))
    }
  }

  /**
   * Handles the Sign Up action.
   *
   * @return The result to display.
   */
  def signup = UserAwareAction.async { implicit request =>
    request.identity match {
//      case Some(user) => Future.successful(Redirect(routes.ApplicationController.index))
      case None => Future.successful(Ok(views.html.signup(SignUpForm.form)))
    }
  }

  /**
   * Handles the Sign Out action.
   *
   * @return The result to display.
   */
//  def signout = SecuredAction.async { implicit request =>
//    env.eventBus.publish(LogoutEvent(request.identity, request, request2lang))
//    Future.successful(env.authenticatorService.discard(Redirect(routes.ApplicationController.index)))
//  }
}
