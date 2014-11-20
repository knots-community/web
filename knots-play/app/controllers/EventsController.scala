package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Silhouette, Environment}
import models.db.TableDefinitions.{Masseur, Company}
import models._
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.{LocalTime, LocalDate, DateTime}
import play.api.data.Form
import play.api.data.Forms._

import scala.concurrent.Future

/**
 * Created by anton on 10/23/14.
 */

class EventsController @Inject()(implicit val env: Environment[AdminUser, CachedCookieAuthenticator])
  extends Silhouette[AdminUser, CachedCookieAuthenticator] {

  val eventForm = Form(
    mapping(
      "companyId" -> longNumber,
      "date" -> nonEmptyText,
      "startTime" -> nonEmptyText,
      "endTime" -> nonEmptyText,
      "masseurs" -> seq(longNumber)
    )((companyId, date, startTime, endTime, masseurs) => {
      Events(companyId, date, startTime, endTime, masseurs)
    })
      ((e: Event) => Some(e.companyId, e.date, e.start, e.end, e.masseurs))
  )

  def list = SecuredAction { implicit request =>
    val result = Events.list
    Ok(views.html.admin.events.list(Some(request.identity), result))
  }

  def save = SecuredAction { implicit request =>
    val form = eventForm.bindFromRequest()
    val s = form.errors.toString()
    if (form.hasErrors) {
      import play.Logger
      Logger.error(form.data.toString());
      Logger.error(form.errors.toString());
    }
    form.fold(
      hasErrors = { form =>
        import play.api.mvc.Flash
        Redirect(routes.EventsController.add()).flashing(Flash(form.data))
      },
      success = { c =>
        val date = LocalDate.parse(c.date, DateTimeFormat.forPattern("MM-dd-yyyy")).toDateTime(LocalTime.now)
        val start = LocalTime.parse(c.start, DateTimeFormat.forPattern("HH:mm"))
        val end = LocalTime.parse(c.end, DateTimeFormat.forPattern("HH:mm"))
        for (mId <- c.masseurs) {
          Reservations.generateTimeSlots(
            date.withTime(start.getHourOfDay, start.getMinuteOfHour, 0, 0),
            date.withTime(end.getHourOfDay, end.getMinuteOfHour, 0, 0),
            mId,
            c.companyId
          )
        }
        Redirect(routes.EventsController.list())
      }
    )
  }

  def add = SecuredAction { implicit request =>
    val form = if (request.flash.get("error").isDefined) eventForm.bind(request.flash.data) else eventForm
    val masseurs = Masseurs.getAllMasseurs
    val companies = CompaniesDao.findAll
    Ok(views.html.admin.events.add(Some(request.identity), form, companies, masseurs))
  }

  def edit = SecuredAction { implicit request =>
    Redirect(routes.EventsController.list())
  }

  def delete = SecuredAction { implicit request =>
    Redirect(routes.EventsController.list())
  }

  def find = SecuredAction { implicit request =>
    Redirect(routes.EventsController.list())
  }
}
