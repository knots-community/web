package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Environment, Silhouette}
import models._
import models.db.TableDefinitions.Event
import org.joda.time.format.DateTimeFormat
import org.joda.time.{LocalDate, LocalTime}
import play.Logger
import play.api.data.Form
import play.api.data.Forms._

/**
 * Created by anton on 10/23/14.
 */

class EventsController @Inject()(implicit val env: Environment[AdminUser, CachedCookieAuthenticator])
  extends Silhouette[AdminUser, CachedCookieAuthenticator] {

  val eventForm = Form(
    mapping(
      "companyId" -> longNumber,
      "date" -> jodaDate,
      "startTime" -> jodaDate,
      "endTime" -> jodaDate,
      "eventType" -> nonEmptyText,
      "masseurs" -> seq(longNumber)
    )((companyId, date, startTime, endTime, eventType, masseurs) => {
      (Event(Some(companyId), date, startTime, endTime, companyId, eventType), masseurs)
    })
      ((e: Event, m: Seq[Long]) => Some(e.companyId, e.date, e.start, e.end, e.eventType, m))
  )

  def list = SecuredAction { implicit request =>
    val result = Events.list
    Ok(views.html.admin.events.list(Some(request.identity), result))
  }

  def save = SecuredAction { implicit request =>
    val form = eventForm.bindFromRequest()
    val s = form.errors.toString()
    if (form.hasErrors) {

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

  def edit(id: Long) = SecuredAction { implicit request =>
    Redirect(routes.EventsController.list())
  }

  def delete(id: Long) = SecuredAction { implicit request =>
    Redirect(routes.EventsController.list())
  }

  def view(id: Long) = SecuredAction { implicit request =>
    Redirect(routes.EventsController.list())
  }
}
