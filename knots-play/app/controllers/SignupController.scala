package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core._
import com.mohiva.play.silhouette.core.exceptions.AuthenticationException
import com.mohiva.play.silhouette.core.providers._
import com.mohiva.play.silhouette.core.services.AuthInfoService
import com.mohiva.play.silhouette.core.utils.PasswordHasher
import forms.SignUpForm
import models.auth.User
import models.services.UserService
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action

import scala.concurrent.Future

/**
 * The sign up controller.
 *
 * @param env The Silhouette environment.
 * @param userService The user service implementation.
 * @param authInfoService The auth info service implementation.
 * @param passwordHasher The password hasher implementation.
 */
class SignupController @Inject() (
  implicit val env: Environment[User, CachedCookieAuthenticator],
  val userService: UserService,
  val authInfoService: AuthInfoService,
  val passwordHasher: PasswordHasher)
  extends Silhouette[User, CachedCookieAuthenticator] {

  /**
   * Registers a new user.
   *
   * @return The result to display.
   */
  def signup = Action.async { implicit request =>
    SignUpForm.form.bindFromRequest.fold (
      form => Future.successful(BadRequest(views.html.signup(form))),
      data => {
        import models.RegularUserRole
        import models.RoleType._
        val loginInfo = LoginInfo(CredentialsProvider.Credentials, data.email)
        val authInfo = passwordHasher.hash(data.password)
        val user = User(
          id = None,
          loginInfo = loginInfo,
          firstName = Some(data.firstName),
          lastName = Some(data.lastName),
          email = Some(data.email),
          RegularUserRole
        )
        for {
          user <- userService.save(user.copy())
          authInfo <- authInfoService.save(loginInfo, authInfo)
          maybeAuthenticator <- env.authenticatorService.create(user)
        } yield {
          maybeAuthenticator match {
            case Some(authenticator) =>
              env.eventBus.publish(SignUpEvent(user, request, request2lang))
              env.eventBus.publish(LoginEvent(user, request, request2lang))
              env.authenticatorService.send(authenticator, Redirect(routes.ApplicationController.index))
            case None => throw new AuthenticationException("Couldn't create an authenticator")
          }
        }
      }
    )
  }
}
