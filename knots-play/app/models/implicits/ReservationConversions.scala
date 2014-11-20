package models.implicits

import models._
import models.db.TableDefinitions.ReservationType

/**
 * Created by anton on 11/17/14.
 */
object ReservationConversions {

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
}
