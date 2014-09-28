package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core._
import com.mohiva.play.silhouette.core.exceptions.AuthenticationException
import com.mohiva.play.silhouette.core.providers._
import com.mohiva.play.silhouette.core.services.AuthInfoService
import com.mohiva.play.silhouette.core.utils.PasswordHasher
import forms.SignUpForm
import models.AdminUser
import models.services.AdminService
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action

import scala.concurrent.Future

/**
 * The sign up controller.
 *
 * @param env The Silhouette environment.
 * @param adminService The user service implementation.
 * @param authInfoService The auth info service implementation.
 * @param passwordHasher The password hasher implementation.
 */
class AdminSignupController @Inject()(implicit val env: Environment[AdminUser, CachedCookieAuthenticator],
                                      val adminService: AdminService, val authInfoService: AuthInfoService,
                                      val passwordHasher: PasswordHasher)
    extends Silhouette[AdminUser, CachedCookieAuthenticator] {

  /**
   * Registers a new user.
   *
   * @return The result to display.
   */
  def signup = Action.async { implicit request =>
    SignUpForm.form.bindFromRequest.fold(form => Future.successful(BadRequest(views.html.admin.signup(form))), data => {
      val loginInfo = LoginInfo(CredentialsProvider.Credentials, data.email)
      val authInfo = passwordHasher.hash(data.password)
      val admin = AdminUser(id = None, loginInfo = loginInfo, firstName = data.firstName, lastName = data.lastName, email = data.email, userId = None)
      for {admin <- adminService.save(admin.copy())
           authInfo <- authInfoService.save(loginInfo, authInfo)
           maybeAuthenticator <- env.authenticatorService.create(admin)} yield {
        maybeAuthenticator match {
          case Some(authenticator) =>
            env.eventBus.publish(SignUpEvent(admin, request, request2lang))
            env.eventBus.publish(LoginEvent(admin, request, request2lang))
            env.authenticatorService.send(authenticator, Redirect(routes.AdminController.index))
          case None => throw new AuthenticationException("Couldn't create an authenticator")
        }
      }
    })
  }
}
