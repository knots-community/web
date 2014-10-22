package models

import java.sql.Timestamp

import org.joda.time.DateTimeZone._
import org.joda.time._
import models.db.TableDefinitions.{TimeSlot, MassageReservation, ReservationType}
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import org.joda.time.{DateTime, DateTimeComparator, DateTimeZone}
import com.github.tototoshi.slick.PostgresJodaSupport._
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import Q.interpolation

/**
 * Created by anton on 9/20/14.
 */
sealed trait ReservationTypeEnum

case object Regular extends ReservationTypeEnum

case object Cancelled extends ReservationTypeEnum

case object NoShow extends ReservationTypeEnum

case object Completed extends ReservationTypeEnum

case object Unavailable extends ReservationTypeEnum

object ReservationTypeEnum extends Dao {
  implicit def typeToDbType(rt: ReservationTypeEnum): ReservationType = {
    rt match {
      case Regular => ReservationType(Some(0), "regular", Some("booking in advance"))
      case Cancelled => ReservationType(Some(1), "cancelled", Some("a cancelled reservation"))
      case NoShow => ReservationType(Some(2), "no show", Some("user didn't attend a reserved massage"))
      case Completed => ReservationType(Some(3), "completed", Some("a successfully completed massage"))
      case Unavailable => ReservationType(Some(4), "unavailable", Some("marks masseur as unavailable"))
    }
  }

  implicit def convertFromDb(rt: ReservationType): ReservationTypeEnum = {
    rt match {
      case ReservationType(Some(1), _, _) => Regular
      case ReservationType(Some(2), _, _) => Cancelled
      case ReservationType(Some(3), _, _) => NoShow
      case ReservationType(Some(4), _, _) => Completed
      case ReservationType(Some(5), _, _) => Unavailable
    }
  }

  implicit def typeToLong(rt: ReservationTypeEnum): Long = {
    rt match {
      case Regular => 1
      case Cancelled => 2
      case NoShow => 3
      case Completed => 4
      case Unavailable => 5
    }
  }

  def initialize = DB withSession { implicit session =>
    reservationTypes += Regular
    reservationTypes += Cancelled
    reservationTypes += NoShow
    reservationTypes += Completed
    reservationTypes += Unavailable
  }

}

object Reservations extends Dao {

  implicit val jodaType = MappedColumnType.base[DateTime, Timestamp](
  { d => new Timestamp(d.getMillis)}, { d => new DateTime(d.getTime, UTC)}
  )
  private val dateComparator = DateTimeComparator.getInstance()

  implicit def dateTimeToScalaWrapper(dt: DateTime): DateTimeWrapper = new DateTimeWrapper(dt)

  def generateTimeSlots(start: DateTime, end: DateTime, masseurId: Long, timeSlotLength: Int = 20) = DB withTransaction { implicit sessison =>
    var time = start
    do {
      timeSlots += TimeSlot(None, masseurId, time, 0)
      time = time.plusMinutes(timeSlotLength)
    } while (time <= end)
  }

  def removeTimeSlots(start: DateTime, end: DateTime, masseurId: Long): Boolean = DB withTransaction { implicit session =>
    (for {
      tt <- timeSlots if tt.masseurId === masseurId && tt.startTime >= start && tt.startTime <= end
    } yield (tt)).delete > 0
  }

  def makeReservation(userId: Long, masseurId: Long, massageType: Long, slotId: Long): Boolean = DB withTransaction { implicit session =>
    timeSlots.filter(_.id === slotId).map(x => (x.status)).update((1))
    (reservations += MassageReservation(None, userId, masseurId, DateTime.now, slotId, 0, None, Regular, massageType, slotId)) > 0
  }

  def findTimeSlots(start: DateTime, end: DateTime) = DB withSession { implicit session =>
    (for {
      ts <- timeSlots if ts.startTime <= start && ts.startTime < end && ts.status === 0
      mass <- masseurs if ts.masseurId === mass.id
    } yield (ts, mass)).list
  }

  case class ScheduleEntry(date: String, title: String, masseurId: Long, allDay: Boolean = true)
  implicit val getSchdeduleResult = GetResult(r => ScheduleEntry(r.<<, r.<<, r.<<))

  def findSchedule(start: DateTime, end: DateTime) : List[ScheduleEntry] = DB withSession { implicit session =>
    val startString = start.toDateTimeISO.toString
    val endString = end.toDateTimeISO.toString
    val q = sql"""select DISTINCT (date_trunc('day', time_slots."startTime")), users."firstName"|| ' ' || users."lastName", masseurs."id" FROM time_slots, masseurs, users WHERE (time_slots."startTime" >= TIMESTAMP '#$startString') AND (time_slots."startTime" < TIMESTAMP '#$endString') AND (time_slots."masseur_id" = masseurs."id") and (masseurs."userId" = users."id")""".as[ScheduleEntry]
    q.list
  }


  class DateTimeWrapper(dt: DateTime) extends Ordered[DateTime] with Ordering[DateTime] {
    def compare(that: DateTime): Int = dateComparator.compare(dt, that)

    def compare(a: DateTime, b: DateTime): Int = dateComparator.compare(a, b)
  }


}