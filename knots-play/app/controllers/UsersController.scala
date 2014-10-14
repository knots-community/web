package controllers

import models.Users
import models.db.TableDefinitions.User
import play.api.mvc.Controller
import utils.auth.Authentication
import play.api.libs.json._

/**
 * Created by anton on 10/8/14.
 */
class UsersController extends Controller with Authentication {

  implicit val jsonFormat = Json.format[User]

  def authUser = AuthenticateMe { userId =>
    Users.findById(userId) map {
      user => Ok(Json.toJson(user))
    } getOrElse(NotFound)

  }

}