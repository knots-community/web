package models

import models.db.Dao
import models.db.TableDefinitions.{TimeSlot, MassageReservation, Event}
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import org.joda.time.DateTime
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import Q.interpolation
import implicits.ReservationConversions._
import utils.db.DateTimeConversion._
import utils.db.PostgressFunctions._

/**
 * Created by anton on 9/20/14.
 */
sealed trait ReservationTypeEnum
case object Regular extends ReservationTypeEnum
case object Cancelled extends ReservationTypeEnum
case object NoShow extends ReservationTypeEnum
case object Completed extends ReservationTypeEnum
case object Unavailable extends ReservationTypeEnum


object Reservations extends Dao {

  def makeReservation(userId: Long, masseurId: Long, massageType: Long, slotId: Long): Boolean = DB withTransaction { implicit session =>
    timeSlots.filter(_.id === slotId).map(x => (x.status)).update((1))
    (reservations += MassageReservation(None, userId, masseurId, DateTime.now, 0, None, Regular, massageType, slotId)) > 0
  }

  case class ScheduleEntry(date: String, title: String, masseurId: Long, allDay: Boolean = true)
  implicit val getScheduleResult = GetResult(r => ScheduleEntry(r.<<, r.<<, r.<<))

  def findSchedule(start: DateTime, end: DateTime): List[ScheduleEntry] = DB withSession { implicit session =>
    val startString = start.toDateTimeISO.toString
    val endString = end.toDateTimeISO.toString
    val q = sql"""select DISTINCT (date_trunc('day', time_slots."startTime")), users."firstName"|| ' ' || users."lastName", masseurs."id" FROM time_slots, masseurs, users WHERE (time_slots."startTime" >= TIMESTAMP '#$startString') AND (time_slots."startTime" < TIMESTAMP '#$endString') AND (time_slots."masseurId" = masseurs."id") and (masseurs."userId" = users."id")""".as[ScheduleEntry]
    q.list
  }

  def initialize = DB withSession { implicit session =>
    reservationTypes += Regular
    reservationTypes += Cancelled
    reservationTypes += NoShow
    reservationTypes += Completed
    reservationTypes += Unavailable
  }

}