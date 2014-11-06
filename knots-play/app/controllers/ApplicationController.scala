package controllers

import models.{CompaniesDao, Users}
import models.db.TableDefinitions.{Company, User}
import models.js.{Signup, LoginCredentials}
import play.api.Routes
import play.api.cache.Cached
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{Action, Controller, Cookie}
import play.api.cache._
import utils.auth.{Auth, AuthUtils, TokenService}
import play.api.Play.current
import sys.process._


class ApplicationController extends Controller with Auth {

  /** JSON reader for [[LoginCredentials]]. */
  implicit val LoginCredentialsFromJson = (
    (__ \ "email").read[String](minLength[String](5)) ~
      (__ \ "password").read[String](minLength[String](2))
    )((email, password) => LoginCredentials(email, password))


  /** JSON reader for [[LoginCredentials]]. */
  implicit val SignupFromJson = (
    (__ \ "firstName").read[String](minLength[String](2)) ~
      (__ \ "lastName").read[String](minLength[String](2)) ~
      (__ \ "email").read[String](minLength[String](5)) ~
      (__ \ "password").read[String](minLength[String](2)) ~
      (__ \ "companyName").read[String]
    )((firstName, lastName, email, password, companyName) => Signup(firstName, lastName, email, password, companyName))
  /**
   * Retrieves all routes via reflection.
   * http://stackoverflow.com/questions/12012703/less-verbose-way-of-generating-play-2s-javascript-router
   * @todo If you have controllers in multiple packages, you need to add each package here.
   */
  val routeCache = {
    val jsRoutesClass = classOf[routes.javascript]
    val controllers = jsRoutesClass.getFields.map(_.get(null))
    controllers.flatMap {
      controller =>
        controller.getClass.getDeclaredMethods.map {
          action =>
            action.invoke(controller).asInstanceOf[play.core.Router.JavascriptReverseRoute]
        }
    }
  }

  def index = Cached("index") {
    Action {
      Ok(views.html.main())
    }
  }

  def signin = Action(parse.json) {
    implicit request =>
      request.body.validate[LoginCredentials].fold(
      errors => {
        BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors)))
      }, {
        credentials => Users.find(credentials.email, credentials.password) map (
          user => {
            val token = TokenService.serialize(TokenService.create(credentials.email))
            Cache.set(token, user.id.get)
            Ok(Json.obj("token" -> token))
              .withCookies(Cookie(AuthUtils.AuthTokenCookieKey, token, None, httpOnly = false))
          }
          ) getOrElse {
          BadRequest(Json.obj("status" -> "KO", "message" -> "User not registered"))
        }
      }
      )
  }

  def signup = Action(parse.json) {
    implicit request =>
      request.body.validate[Signup].fold(
      errors => {
        BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors)))
      }, {
        data => {
          val c = CompaniesDao.findByName(data.companyName)
          Users.save(User(None, data.firstName, data.lastName, data.email, c.id.get), data.password) map (
            newUser => {
              val token = TokenService.create(data.email)
              val tokenStr = TokenService.serialize(token)
              Cache.set(tokenStr, newUser.id.get)
              val iii = Cache.getAs[Long](tokenStr)
              Ok(Json.obj("token" -> token))
                .withCookies(Cookie(AuthUtils.AuthTokenCookieKey, tokenStr, None, httpOnly = false))
            }
            ) getOrElse (BadRequest(Json.obj("status" -> "KO", "message" -> "user already exists")))
        }
      })
  }

  case class CompanyName(companyKey: String)

  implicit val companyNameJson = Json.format[CompanyName]
  implicit val companyJson = Json.format[Company]

  def getCompanyInfo = Action(parse.json) { implicit request =>
    request.body.validate[CompanyName].fold(
    errors => {
      BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors)))
    }, {
      data => {
        val c = CompaniesDao.findBySignupLink(data.companyKey)
        Ok(Json.obj("status" -> "OK", "company" -> c))
      }
    }
    )
  }

  def getCompanyInfoById(id: Long) = SecuredAction { implicit request =>
    val company = CompaniesDao.findById(id)
    Ok(Json.obj("status" -> "OK", "company" -> company))
  }

  /**
   * Returns the JavaScript router that the client can use for "type-safe" routes.
   * Uses browser caching; set duration (in seconds) according to your release cycle.
   * @param varName The name of the global variable, defaults to `jsRoutes`
   */
  def jsRoutes(varName: String = "jsRoutes") = Cached(_ => "jsRoutes", duration = 86400) {
    Action {
      implicit request =>
        Ok(Routes.javascriptRouter(varName)(routeCache: _*)).as(JAVASCRIPT)
    }
  }

  def deploy = Action(parse.json) { request =>
    ("cd /src/web/knots-play" #| "git pull" !)
    Ok("GOTCHA")
  }

  def main(page: String) = Action { request =>
    Ok(views.html.main())
  }
}
