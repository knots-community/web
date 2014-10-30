package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Silhouette, Environment}
import models.db.TableDefinitions.Company
import models.{Masseurs, MasseurProfile, CompaniesDao, AdminUser}
import play.api.data.Form
import play.api.data.Forms._

import scala.concurrent.Future

/**
 * Created by anton on 10/23/14.
 */
class CompaniesController @Inject()(implicit val env: Environment[AdminUser, CachedCookieAuthenticator])
  extends Silhouette[AdminUser, CachedCookieAuthenticator] {

  val companyForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "name" -> nonEmptyText(2),
      "address" -> nonEmptyText(2),
      "phone" -> nonEmptyText(2),
      "email" -> email
    )((id, name, address, phone, email) => {
      Company(id, name, address, phone, email, None)
    })
      ((c: Company) => Some(c.id, c.name, c.address, c.phone, c.contactEmail))
  )

  def list = SecuredAction { implicit request =>
    val companies = CompaniesDao.findAll
    Ok(views.html.admin.companies.list(Some(request.identity), companies))
  }

  def find(id: Long) = SecuredAction { implicit request =>
    val company = CompaniesDao.findById(id)
    val genericSignupUrl = "http://" + request.host + "/signup/";
    Ok(views.html.admin.companies.view(Some(request.identity), company, genericSignupUrl))
  }

  def save = SecuredAction { implicit request =>
    val form = companyForm.bindFromRequest()
    val s = form.errors.toString()
    if (form.hasErrors) {
      import play.Logger
      Logger.error(form.data.toString());
      Logger.error(form.errors.toString());
    }
    form.fold(
      hasErrors = { form =>
        import play.api.mvc.Flash
        Redirect(routes.CompaniesController.add()).flashing(Flash(form.data))
      },
      success = { c =>
        CompaniesDao.add(c)
        Redirect(routes.CompaniesController.list())
      }
    )
  }

  def update(id: Long) = SecuredAction { implicit request =>
    companyForm.bindFromRequest().fold(
      hasErrors = { form => import play.api.mvc.Flash
        Redirect(routes.CompaniesController.edit(id)).flashing(Flash(form.data))
      },
      success = { c =>
        CompaniesDao.update(c)
        Redirect(routes.CompaniesController.list())
      }
    )
  }

  def delete(id: Long) = SecuredAction { implicit request =>
    CompaniesDao.delete(id)
    Redirect(routes.CompaniesController.list())
  }

  def edit(id: Long) = SecuredAction { implicit request =>
    val company = CompaniesDao.findById(id)
    val form = companyForm.fill(company)
    Ok(views.html.admin.companies.edit(Some(request.identity), form, id))
  }

  def add = SecuredAction { implicit request =>
    val form = if (request.flash.get("error").isDefined) companyForm.bind(request.flash.data) else companyForm
    Ok(views.html.admin.companies.add(Some(request.identity), form))
  }
}
