package controllers

import java.sql.Timestamp

import models.Reservations
import models.js.{TimeSlotsForDay, Booking, LoginCredentials}
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.mvc.Action
import utils.auth.Auth
import play.api.libs.functional.syntax._

/**
 * Created by anton on 10/13/14.
 */
class BookingController extends Auth {

  val DefaultBookingLength = 20 * 60 * 1000;
  // 20 minutes

  /** JSON reader for [[LoginCredentials]]. */
  implicit val BookingFromJson = (
    (__ \ "masseurId").read[Long] ~
      (__ \ "time").read[Long]
    )((masseurId, time) => Booking(masseurId, new DateTime(time)))

  implicit val dayReads: Reads[TimeSlotsForDay] = (JsPath \ "day").read[Long].map(x => TimeSlotsForDay(new DateTime(x)))

//  def timeSlots = SecuredAction(parse.json) { implicit request =>
  def timeSlots = Action { implicit request =>
//    val req = request.body
//    req.validate[TimeSlotsForDay].fold(
//    (errors => BadRequest(Json.obj("status" -> "fail", "message" -> JsError.toFlatJson(errors)))
//    ), (
//      dayInfo => {
//         val slots = Reservations.findTimeSlots(dayInfo.day.withTimeAtStartOfDay(), dayInfo.day.withTime(23, 0, 0, 0))
        Ok(Json.obj("status" -> "OK", "timeSlots" -> ""))
//      }
//  ))
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