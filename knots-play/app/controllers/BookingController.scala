package controllers

import models.Reservations.SlotEntry
import models.db.TableDefinitions.Company
import models.{Masseurs, Users, CompaniesDao, Reservations}
import models.js.{Booking, LoginCredentials}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{LocalDate, DateTime}
import play.api.Logger
import play.api.libs.json.Reads._
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
  implicit val bookingFromJson = (
    (__ \ "masseurId").read[Long] ~
      (__ \ "slotId").read[Long]
    )((masseurId, slotId) => Booking(masseurId, slotId))


  /** JSON reader for [[LoginCredentials]]. */
  implicit val SlotJson = Json.format[SlotEntry]

  case class SlotInfo(id: Long, startTime: String)
  case class MasseurInfo(name: String, masseurId: Long)
  case class MasseurSlots(masseurInfo: MasseurInfo, slots: ListBuffer[SlotInfo])
  case class Event(date: LocalDate, masseurSlots: ListBuffer[MasseurSlots])
  case class Events(events: ListBuffer[Event])

  implicit val slotInfoJson = Json.format[SlotInfo]
  implicit val masseurInfoJson = Json.format[MasseurInfo]
  implicit val masseurSlotsJson = Json.format[MasseurSlots]
  implicit val eventJson = Json.format[Event]
  implicit val eventsJson = Json.format[Events]


  def timeSlots = SecuredAction { implicit request =>
    val user = Users.findById(request.userId).get
    val company = CompaniesDao.findById(user.companyId)

    val slotTimeFormat = DateTimeFormat.forPattern("HH:mm")
    val events = Events(ListBuffer())
    val dates = Reservations.findDatesForCompany(company.id.get)
    for (d <- dates) {
      val parsedDate = DateTime.parse(d, DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss"))
      events.events += Event(parsedDate.toLocalDate, ListBuffer())
      val slots = Reservations.findTimeSlots(parsedDate, parsedDate.withTime(20, 0, 0, 0), company.id.get)

      var currentMasseur: String = ""
      for (s <- slots) {
        if (s.masseurName != currentMasseur) {
          events.events.last.masseurSlots += MasseurSlots(MasseurInfo(s.masseurName, s.masseurId), ListBuffer())
          currentMasseur = s.masseurName
        }

        val parsedTime = DateTime.parse(s.startTime, DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss"))
        events.events.last.masseurSlots.last.slots += SlotInfo(s.slotId, slotTimeFormat.print(parsedTime))
      }
    }

    Logger.debug(Json.toJson(events).toString)

    Ok(Json.obj("status" -> "OK", "slots" -> events)).as(JSON)
  }

  case class BookingInfo(companyName: String, address: String, time: String, masseurName: String)

  implicit val bookingInfoJson = Json.format[BookingInfo]

  def performBooking = SecuredAction(parse.json) { implicit request =>
    val b = request.body.toString;
    request.body.validate[Booking].fold((errors => BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors)))),
      (bookingInfo => {
        val user = Users.findById(request.userId).get
        val company = CompaniesDao.findById(user.companyId)
        val masseur = Masseurs.find(bookingInfo.masseurId)
        val startTime = Reservations.findSlotTime(bookingInfo.slotId)

        Reservations.makeReservation(user.id.get, bookingInfo.masseurId, 1, bookingInfo.slotId)
        Ok(Json.obj("status" -> "OK", "bookingInfo" -> BookingInfo(company.name, company.address, startTime.toDateTimeISO.toString, masseur.firstName + " " + masseur.lastName))).as(JSON)
      }))
  }

  implicit val companyJson = Json.format[Company]

  def getCompanyInfo = SecuredAction { implicit request =>
    val userId = request.userId;
    val user = Users.findById(userId).get
    val company = CompaniesDao.findById(user.companyId)
    Ok(Json.obj("status" -> "OK", "company" -> company))
  }
}