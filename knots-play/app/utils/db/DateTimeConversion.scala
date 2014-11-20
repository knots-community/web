package utils.db

import java.sql.Timestamp

import org.joda.time.{DateTimeComparator, DateTime}
import org.joda.time.DateTimeZone._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

/**
 * Created by anton on 11/17/14.
 */
object DateTimeConversion {

  private val dateComparator = DateTimeComparator.getInstance()

  implicit val jodaType = MappedColumnType.base[DateTime, Timestamp](
  { d => new Timestamp(d.getMillis)}, { d => new DateTime(d.getTime, UTC)}
  )

  class DateTimeWrapper(dt: DateTime) extends Ordered[DateTime] with Ordering[DateTime] {
    def compare(that: DateTime): Int = dateComparator.compare(dt, that)

    def compare(a: DateTime, b: DateTime): Int = dateComparator.compare(a, b)
  }

  implicit def dateTimeToScalaWrapper(dt: DateTime): DateTimeWrapper = new DateTimeWrapper(dt)
}
