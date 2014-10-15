package controllers

import models.Users
import models.db.TableDefinitions.User
import play.api.mvc.Controller
import utils.auth.{Auth}
import play.api.libs.json._

/**
 * Created by anton on 10/8/14.
 */
class UsersController extends Controller with Auth {

  implicit val jsonFormat = Json.format[User]

  def authUser = SecuredAction {  implicit request =>
    Users.findById(request.userId) map {
          request.body
      user => Ok(Json.toJson(user))
    } getOrElse(NotFound)

  }

}