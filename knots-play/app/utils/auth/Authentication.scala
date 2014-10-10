package utils.auth

import play.api.cache._
import play.api.mvc.{Action, Controller, RequestHeader, Result}
import play.api.Play.current

trait Authentication {
  self: Controller =>
  //  var accessConditions: List[Conditions.Condition] = List.empty

  def AuthenticateMe(f: Long => Result) = Action { implicit request =>
    val userId = AuthUtils.parseUserFromHeader

    if (userId.isEmpty)
      Forbidden("Invalid username or password")
    else {
      //      accessConditions.map(condition => condition(user.get)).collectFirst[String] { case Left(error) => error}
      //      match {
      //        case Some(error) => Forbidden(s"Conditions not met: $error")
      f(userId.get)
    }
  }

  //object Conditions {
  //  type Condition = (User => Either[String, Unit])
  //  def isAdmin:Condition = {
  //    user => if(user.isAdmin)
  //      Right()
  //    else
  //      Left("User must be admin!")
  //  }
}

//trait AdminUsersOnly {
//  self:Authentication =>
//  accessConditions = accessConditions :+ Conditions.isAdmin
//}

object AuthUtils {

  val AuthTokenHeader = "X-XSRF-TOKEN"
  val AuthTokenCookieKey = "XSRF-TOKEN"
  val AuthTokenUrlKey = "auth"


  def parseUserFromHeader(implicit request: RequestHeader): Option[Long] = {
    request.cookies.get(AuthTokenCookieKey) map {
      xsrfTokenCookie => {
        val maybeToken = request.headers.get(AuthTokenHeader)
        maybeToken map {
          token : String => {
            Cache.getAs[Long](token) map { userId: Long =>
              if (xsrfTokenCookie.value.equals(token)) {
                if(TokenService.validate(token)) {
                  Some(userId)
                } else {
                  None
                }
              } else {
                None
              }
            } getOrElse (None)
          }
        } getOrElse None
      }
    } getOrElse None
  }
}