package models

import com.github.tototoshi.slick.PostgresJodaSupport._
import models.db.Dao
import models.db.TableDefinitions.Event
import org.joda.time.DateTime
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import utils.db.PostgressFunctions._

/**
 * Created by anton on 10/23/14.
 */
object Events extends Dao {

  def list = DB withSession { implicit request =>
    events.filter(e => dateTruncate("day", e.start) >= DateTime.now).list
  }

  def findByCompany(companyId: Long) : List[Event] = DB withSession { implicit session =>
    events.sortBy(_.start).filter(e => e.companyId === companyId && dateTruncate("day", e.start) > DateTime.now && dateTruncate("day", e.end) <  DateTime.now.withYear(2050)).list
    //val q = sql"""select DISTINCT (date_trunc('day', time_slots."startTime")) FROM time_slots WHERE (time_slots."startTime" >= TIMESTAMP '#$startString') AND (time_slots."startTime" < TIMESTAMP '#$endString') and (time_slots."companyId" = '#$companyId')""".as[String]
    //    q.list
  }

  def create(companyId: Long, start: DateTime, date: DateTime, end: DateTime, eventType: String = "massage") : Long = DB withSession { implicit session =>
    (events returning events.map(_.id)) += Event(None, date, start, end, companyId, eventType)
  }

  def delete(eventId: Long) : Boolean = DB withSession { implicit session =>
    events.filter(_.id === eventId).delete > 0
  }

  def findById(eventId: Long) : Option[Event] = DB withSession { implicit session =>
    events.filter(_.id === eventId).firstOption
  }
}