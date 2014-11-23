package utils.db

import org.joda.time.DateTime
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

/**
 * Created by anton on 11/17/14.
 */
object PostgressFunctions {
  val date_trunc = SimpleFunction.binary[String, DateTime, DateTime]("date_trunc")
  def dateTruncate(precision: String, dtg: Column[DateTime]): Column[DateTime] = date_trunc(precision, dtg)

}
