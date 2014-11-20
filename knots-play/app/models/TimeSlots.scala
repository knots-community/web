package models

import models.Reservations._
import models.db.Dao
import models.db.TableDefinitions.{TimeSlot, Event}
import org.joda.time.DateTime
import play.api.db.slick._
import utils.db.DateTimeConversion._
import utils.db.PostgressFunctions._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import scala.slick.jdbc.GetResult
import Q.interpolation

/**
 * Created by anton on 11/17/14.
 */
object TimeSlots extends Dao {

  def generateTimeSlotsForEvent(event: Event, masseurId: Long, slotLength: Int = 20) = DB withTransaction { implicit sessison =>
    var time = event.start
    do {
      timeSlots += TimeSlot(None, masseurId, time, time.plusMinutes(slotLength), 0, event.id.get)
      time = time.plusMinutes(slotLength)
    } while (time <= event.end)
  }

  def findSlotTime(slotId: Long) = DB withSession { implicit session =>
    timeSlots.filter(_.id === slotId).map(s => s.startTime).first
  }

  def removeTimeSlots(start: DateTime, end: DateTime, masseurId: Long): Boolean = DB withTransaction { implicit session =>
    timeSlots.filter(tt => tt.masseurId === masseurId && tt.startTime >= start && tt.startTime <= end).delete > 0
  }

  case class SlotEntry(slotId: Long, startTime: String, masseurId: Long, masseurName: String)
  implicit val getSlotEntryResult = GetResult(r => SlotEntry(r.<<, r.<<, r.<<, r.<<))

  def findEventTimeSlots(event: Event): List[SlotEntry] = DB withSession { implicit session =>
    val startString = event.start.toString
    val endString = event.end.toString

    val q = sql"""select time_slots."id", time_slots."startTime", masseurs."id", users."firstName" || ' ' || users."lastName" FROM time_slots, masseurs, users WHERE (time_slots."startTime" >= TIMESTAMP '#$startString') AND (time_slots."startTime" < TIMESTAMP '#$endString') and (time_slots."companyId" = '#$companyId') and (time_slots."masseurId" = masseurs."id") and (masseurs."userId" = users."id") AND (time_slots."status" = 0) ORDER BY (masseurs."id", time_slots."startTime")""".as[SlotEntry]
    q.list
  }

}
