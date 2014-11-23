package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.exceptions.AuthenticationException
import com.mohiva.play.silhouette.core.providers.CredentialsProvider
import com.mohiva.play.silhouette.core.services.AuthInfoService
import com.mohiva.play.silhouette.core._
import com.mohiva.play.silhouette.core.utils.PasswordHasher
import forms.{SignUpForm, SignInForm}
import models.Reservations.ScheduleEntry
import models._
import models.silhouette.services.AdminService
import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Action

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
 * Created by anton on 9/21/14.
 */
class AdminController @Inject() (
                                  implicit val env: Environment[AdminUser, CachedCookieAuthenticator],
                                  val adminService: AdminService,
                                  val authInfoService: AuthInfoService,
                                  val passwordHasher: PasswordHasher)
  extends Silhouette[AdminUser, CachedCookieAuthenticator] {



  implicit val scheduleFormat = Json.format[ScheduleEntry]

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

  def signin = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Redirect(routes.AdminController.index))
      case None => Future.successful(Ok(views.html.admin.signin(SignInForm.form)))
    }
  }

  def signout = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(identity) =>
        env.authenticatorService.retrieve(request).flatMap { authenticator =>
          Future.successful(env.authenticatorService.discard(Redirect(routes.AdminController.index)))
        }
      case None => Future.successful(Redirect(routes.AdminController.index))
    }
  }

  def index = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Redirect(routes.AdminController.listUsers()))
      case None => Future.successful(Ok(views.html.admin.signin(SignInForm.form)))
    }
  }

  def listUsers = UserAwareAction.async { implicit request =>
    val futureUsers = Future {
      Users.getAll
    }
    futureUsers.map(users => Ok(views.html.admin.users.list(request.identity, users)))
  }


  def calendar = UserAwareAction.async { implicit request =>
    Future.successful(Ok(views.html.admin.calendar(request.identity)))
  }

  def schedule = Action { implicit request =>
    val start = request.queryString.get("start").flatMap(_.headOption) getOrElse("invalid")
    val end = request.queryString.get("end").flatMap(_.headOption) getOrElse("invalid")
    val startDate = DateTime.parse(start)
    val endDate = DateTime.parse(end)
    val slots = Reservations.findSchedule(startDate, endDate)
    val res = Json.toJson(slots)
    Ok(res).as(JSON)
  }
}
