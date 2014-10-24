package models

import models.Reservations._
import models.db.TableDefinitions.{Masseurs, Company}
import org.joda.time.DateTime
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import com.github.tototoshi.slick.PostgresJodaSupport._
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import Q.interpolation

/**
 * Created by anton on 10/23/14.
 */
case class Event(companyId: Long, date: String, start: String, end: String, masseurs: Seq[Long])

case class EventInfo(date: String, companyName: String)

object Event extends Dao {

  implicit val getEventInfoResult = GetResult(r => EventInfo(r.<<, r.<<))

  def list() = DB withSession { implicit request =>
    val q = Q.queryNA[EventInfo]("""select DISTINCT (date_trunc('day', time_slots."startTime")), companies."name" FROM time_slots, companies WHERE (time_slots."companyId" = companies."id")""")
    q.list
  }


}