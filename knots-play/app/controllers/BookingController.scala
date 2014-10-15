package controllers

import java.sql.Timestamp

import models.Reservations
import models.js.{Booking, LoginCredentials}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import utils.auth.Auth

/**
 * Created by anton on 10/13/14.
 */
class BookingController extends Auth {

  val DefaultBookingLength = 20 * 60 * 1000;
  // 20 minutes

  /** JSON reader for [[LoginCredentials]]. */
//  implicit val BookingFromJson = (
//    (__ \ "masseurId").read[Long] ~
//      (__ \ "time").read[String]
//    )((masseurId, time) => Booking(masseurId, new Timestamp(time)))


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