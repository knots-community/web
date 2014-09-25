package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core._
import com.mohiva.play.silhouette.core.exceptions.AuthenticationException
import com.mohiva.play.silhouette.core.providers._
import com.mohiva.play.silhouette.core.services.AuthInfoService
import forms.SignInForm
import models.AdminUser
import models.auth.User
import models.services.{AdminService, UserService}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action

import scala.concurrent.Future

/**
 * The credentials auth controller.
 *
 * @param env The Silhouette environment.
 */
class AdminCredentialsAuthController @Inject() (
                                              implicit val env: Environment[AdminUser, CachedCookieAuthenticator],
                                              val adminService: AdminService,
                                              val authInfoService: AuthInfoService)
    extends Silhouette[AdminUser, CachedCookieAuthenticator] {

  /**
   * Authenticates a user against the credentials provider.
   *
   * @return The result to display.
   */
  def authenticate = Action.async { implicit request =>
    SignInForm.form.bindFromRequest.fold (
      form => Future.successful(BadRequest(views.html.admin.signin(form))),
      credentials => (env.providers.get(CredentialsProvider.Credentials) match {
        case Some(p: CredentialsProvider) => p.authenticate(credentials)
        case _ => Future.failed(new AuthenticationException(s"Cannot find credentials provider"))
      }).flatMap { loginInfo =>
        adminService.retrieve(loginInfo).flatMap {
          case Some(admin) => env.authenticatorService.create(admin).map {
            case Some(authenticator) =>
              env.eventBus.publish(LoginEvent(admin, request, request2lang))
              env.authenticatorService.send(authenticator, Redirect(routes.AdminController.index))
            case None => throw new AuthenticationException("Couldn't create an authenticator")
          }
          case None => Future.failed(new AuthenticationException("Couldn't find admin"))
        }
      }.recoverWith(exceptionHandler)
    )
  }
}