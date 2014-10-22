package controllers

import models.Users
import models.db.TableDefinitions.User
import play.api.mvc.{Cookie, Controller}
import utils.auth.{AuthUtils, Auth}
import play.api.libs.json._

/**
 * Created by anton on 10/8/14.
 */
class UsersController extends Controller with Auth {

  implicit val jsonFormat = Json.format[User]

  def authUser = SecuredAction { implicit request =>
    Users.findById(request.userId) map {
      val token = request.headers.get(AuthUtils.AuthTokenHeader)
      user => Ok(Json.toJson(user)).withCookies(Cookie(AuthUtils.AuthTokenCookieKey, token.get, None, httpOnly = false))
    } getOrElse (NotFound)

  }

}