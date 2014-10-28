package controllers

import java.time.format.DateTimeFormatter

import models.Reservations.SlotEntry
import models.{Users, CompaniesDao, Reservations}
import models.js.{Booking, LoginCredentials}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{LocalDate, DateTime}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.Controller
import utils.auth.Auth
import play.api.libs.functional.syntax._

import scala.collection.mutable.ListBuffer

/**
 * Created by anton on 10/13/14.
 */
class BookingController extends Controller with Auth {

  val DefaultBookingLength = 20 * 60 * 1000;
  // 20 minutes

  /** JSON reader for [[LoginCredentials]]. */
  implicit val BookingFromJson = (
    (__ \ "masseurId").read[Long] ~
      (__ \ "time").read[Long]
    )((masseurId, time) => Booking(masseurId, new DateTime(time)))

//  def timeSlots = SecuredAction { implicit request =>
//    val companyId = request.queryString.get("companyId").flatMap(_.headOption).getOrElse(BadRequest(Json.obj("status" -> "fail", "message" -> "Unknown company")))
//    val slots = Reservations.findOpenTimeSlotsForCompany(0)
//    Ok(Json.obj("status" -> "OK", "timeSlots" -> ""))
//  }

  implicit val SlotJson = Json.format[SlotEntry]


  case class SlotInfo(id: Long, startTime: String)
  case class MasseurSlots(name: String, slots: ListBuffer[SlotInfo])
  case class Event(date: LocalDate, masseurSlots : ListBuffer[MasseurSlots])
  case class Events(events: ListBuffer[Event])

  implicit val slotInfoJson = Json.format[SlotInfo]
  implicit val masseurSlotsJson = Json.format[MasseurSlots]
  implicit val eventJson = Json.format[Event]
  implicit val eventsJson = Json.format[Events]

  def timeSlots = SecuredAction { implicit request =>
    val user = Users.findById(request.userId).get
    val company = CompaniesDao.findById(user.companyId)

    val events = Events(ListBuffer())
    val dates = Reservations.findDatesForCompany(company.id.get)
    for(d <- dates) {
      val parsedDate = DateTime.parse(d, DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss"));
      events.events += Event(parsedDate.toLocalDate, ListBuffer())
      val slots = Reservations.findTimeSlots(parsedDate, parsedDate.withTime(20, 0, 0, 0), company.id.get)

      var currentMasseur: String = ""
      for(s <- slots) {
        if(s.masseurName != currentMasseur) {
          events.events.last.masseurSlots += MasseurSlots(s.masseurName, ListBuffer())
          currentMasseur = s.masseurName
        }

        events.events.last.masseurSlots.last.slots += SlotInfo(s.slotId, s.startTime)
      }
    }

    Logger.debug(Json.toJson(events).toString)

    Ok(Json.obj("status" -> "OK", "slots" -> events)).as(JSON)
  }

  def performBooking = SecuredAction(parse.json) { implicit request =>
    val req = request.body
    //    request.body.validate[Booking].fold(
    //    errors => {
    //      BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors)))
    //    }, {
    //      bookingForm => {
    //        val endTime = new Timestamp(bookingForm.startTime.getTime + DefaultBookingLength)
    //        Reservations.makeReservation(request.userId, bookingForm.masseurId, bookingForm.startTime, endTime) map (_ => Ok()) getOrElse(BadRequest(Json.obj("status" -> "KO", "message" -> "Unable to book")))
    //
    //      }
    //    })
    Ok(Json.obj("status" -> "OK", "message" -> req))
  }

}

/*
  fold (
    (BadRequest(Json.obj("status" -> "KO", "message" -> "Unable to book")))
    {
      Ok(Json.obj("status" -> "OK")).as(JSON)
    })
}
})
*/