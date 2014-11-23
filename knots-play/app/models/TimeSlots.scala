package models

import models.db.Dao
import models.db.TableDefinitions.{Event, TimeSlot}
import org.joda.time.DateTime
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import utils.db.DateTimeConversion._

import scala.slick.jdbc.{GetResult}

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

  case class SlotEntry(slotId: Long, startTime: DateTime, masseurId: Long, masseurName: String)

  def findEventTimeSlots(event: Event, futureOnly: Boolean = true): List[SlotEntry] = DB withSession { implicit session =>
    val query = for {
      ts <- timeSlots if ts.eventId === event.id
      masseur <- masseurs if masseur.id === ts.masseurId
      u <- users if masseur.userId === u.id
    } yield (ts.id, ts.startTime, masseur.id, u.firstName + " " + u.lastName)

    query.list.map(x => SlotEntry(x._1, x._2, x._3, x._4))
  }

}
