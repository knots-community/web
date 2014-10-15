package utils.auth

import play.api.Play.current
import play.api.cache._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future


trait Auth extends Controller  {
  /**
   * A request that only allows access if a user is authorized.
   */
  case class SecuredRequest[A](userId: Long, request: Request[A]) extends WrappedRequest(request)

  /**
   * A request that adds the User for the current call.
   */
  case class RequestWithUser[A](userId: Option[Long], request: Request[A]) extends WrappedRequest(request)

  class SecuredActionBuilder(authorize: Option[Long] = None) extends ActionBuilder[SecuredRequest] {

    override def invokeBlock[A](request: Request[A], block: (SecuredRequest[A]) => concurrent.Future[Result]): concurrent.Future[Result] = {
      implicit val req = request

      AuthUtils.parseUserFromHeader map (userId => block(SecuredRequest(userId, request))) getOrElse Future.successful((Unauthorized(Json.obj("success" -> false, "message" -> "access forbidden"))))
    }
  }

  object SecuredAction extends SecuredActionBuilder {

    /**
     * Creates a secured action.
     */
    def apply() = new SecuredActionBuilder(None)

    /**
     * Creates a secured action.
     *
     * @param authorize An Authorize object that checks if the user is authorized to invoke the action.
     */
    def apply(authorize: Long) = new SecuredActionBuilder(Some(authorize))
  }

}

object AuthUtils {

  val AuthTokenHeader = "X-XSRF-TOKEN"
  val AuthTokenCookieKey = "XSRF-TOKEN"
  val AuthTokenUrlKey = "auth"


  def parseUserFromHeader(implicit request: RequestHeader): Option[Long] = {
    for {
      maybeToken <- request.headers.get(AuthTokenHeader)
      authInfo <- TokenService.deserialize(maybeToken)
      userId <- Cache.getAs[Long](maybeToken)
    } yield userId
  }
}