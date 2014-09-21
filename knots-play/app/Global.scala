import com.google.inject.Injector
import com.kenshoo.play.metrics.MetricsFilter
import com.mohiva.play.silhouette.core.{Logger, SecuredSettings}
import controllers.routes
import play.api.GlobalSettings
import play.api.i18n.{Lang, Messages}
import play.api.mvc.Results._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import com.google.inject.Guice
import utils.LoggingFilter
import utils.di.SilhouetteModule
import scala.concurrent.Future
import play.api.db.slick._
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * The global configuration.
 */
object Global extends WithFilters(LoggingFilter, MetricsFilter) with SecuredSettings {

  /**
   * The Guice dependencies injector.
   */
  var injector: Injector = _

  override def onStart(app: play.api.Application) = {
    super.onStart(app)
    // Now the configuration is read and we can create our Injector.
    injector = Guice.createInjector(new SilhouetteModule())
    Future {
      DB withSession {
        import models.db.TableDefinitions.DbRole
        implicit session =>
        import models._
          Roles.insert(RegularUserRole)
        Some(Roles.insert(AdminRole))
        Roles.insert(ProviderRole)
        Roles.insert(MasseurRole)
//        Admins.insert(AdminUser(None, ))
      }
    }
  }

  /**
   * Loads the controller classes with the Guice injector,
   * in order to be able to inject dependencies directly into the controller.
   *
   * @param controllerClass The controller class to instantiate.
   * @return The instance of the controller class.
   * @throws Exception if the controller couldn't be instantiated.
   */
  override def getControllerInstance[A](controllerClass: Class[A]) = injector.getInstance(controllerClass)

  /**
   * Called when a user is not authenticated.
   *
   * As defined by RFC 2616, the status code of the response should be 401 Unauthorized.
   *
   * @param request The request header.
   * @param lang The currently selected language.
   * @return The result to send to the client.
   */
  override def onNotAuthenticated(request: RequestHeader, lang: Lang): Option[Future[Result]] = {
    Some(Future.successful(Redirect(routes.ApplicationController.signin)))
  }

  /**
   * Called when a user is authenticated but not authorized.
   *
   * As defined by RFC 2616, the status code of the response should be 403 Forbidden.
   *
   * @param request The request header.
   * @param lang The currently selected language.
   * @return The result to send to the client.
   */
  override def onNotAuthorized(request: RequestHeader, lang: Lang): Option[Future[Result]] = {
    Some(Future.successful(Redirect(routes.ApplicationController.signin).flashing("error" -> Messages("access.denied"))))
  }
}
