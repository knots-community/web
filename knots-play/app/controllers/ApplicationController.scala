package controllers

import models.Users
import models.js.LoginCredentials
import play.api.Routes
import play.api.cache.Cached
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{Action, Controller, Cookie}
import play.cache.Cache
import utils.auth.{AuthUtils, Authentication, TokenService}
import play.api.Play.current


class ApplicationController extends Controller with Authentication {

  /** JSON reader for [[LoginCredentials]]. */
  implicit val LoginCredentialsFromJson = (
    (__ \ "email").read[String](minLength[String](5)) ~
      (__ \ "password").read[String](minLength[String](2))
    )((email, password) => LoginCredentials(email, password))

  def index = Action {
    Ok(views.html.main())
  }

  def signin = Action(parse.json) { implicit request =>
    request.body.validate[LoginCredentials].fold(
    errors => {
      BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors)))
    }, {
      credentials => Users.find(credentials.email, credentials.password) map (
        user => {
          val token = TokenService.serialize(TokenService.create(credentials.email, credentials.password))
          Cache.set(token, user.id)
          Ok(Json.obj("token" -> token))
            .withCookies(Cookie(AuthUtils.AuthTokenCookieKey, token, None, httpOnly = false))
        }
        ) getOrElse (BadRequest(Json.obj("status" -> "KO", "message" -> "User not registered")))
    }
    )
  }

  /**
   * Retrieves all routes via reflection.
   * http://stackoverflow.com/questions/12012703/less-verbose-way-of-generating-play-2s-javascript-router
   * @todo If you have controllers in multiple packages, you need to add each package here.
   */
  val routeCache = {
    val jsRoutesClass = classOf[routes.javascript]
    val controllers = jsRoutesClass.getFields.map(_.get(null))
    controllers.flatMap { controller =>
      controller.getClass.getDeclaredMethods.map { action =>
        action.invoke(controller).asInstanceOf[play.core.Router.JavascriptReverseRoute]
      }
    }
  }


  /**
   * Returns the JavaScript router that the client can use for "type-safe" routes.
   * Uses browser caching; set duration (in seconds) according to your release cycle.
   * @param varName The name of the global variable, defaults to `jsRoutes`
   */
  def jsRoutes(varName: String = "jsRoutes") = Cached(_ => "jsRoutes", duration = 86400) {
    Action { implicit request =>
      Ok(Routes.javascriptRouter(varName)(routeCache: _*)).as(JAVASCRIPT)
    }
  }
  //    def book = SecuredAction { implicit request =>
  //      Ok(views.html.book(request.identity, ))
  //    }

  /**
   * Handles the Sign In action.
   *
   * @return The result to display.
   */
  //  def signin = UserAwareAction.async { implicit request =>
  //    request.identity match {
  //      case Some(user) => Future.successful(Redirect(routes.ApplicationController.index))
  //      case None => Future.successful(Ok(views.html.signin(SignInForm.form)))
  //    }
  //  }

  /**
   * Handles the Sign Up action.
   *
   * @return The result to display.
   */
  //  def signup = UserAwareAction.async { implicit request =>
  //    request.identity match {
  //      case Some(user) => Future.successful(Redirect(routes.ApplicationController.index))
  //      case None => Future.successful(Ok(views.html.signup(SignUpForm.form)))
  //    }
  //  }

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
