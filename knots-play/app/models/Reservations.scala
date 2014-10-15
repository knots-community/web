package models

import java.sql.Timestamp

import models.db.TableDefinitions.{MassageReservation, ReservationType}
import org.joda.time.DateTime
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

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

  implicit def convertFromDb(rt: ReservationType) : ReservationTypeEnum = {
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

  val minDateTime = new Timestamp(0)
  val maxDateTime = new Timestamp(Long.MaxValue)

  def markUnavailable(masseurId: Long, start: Timestamp, end: Timestamp) = DB.withSession { implicit session =>
    reservations += MassageReservation(None, 0, masseurId, minDateTime, minDateTime, maxDateTime, 0, None, Unavailable, Swedish)
  }

  def makeAvailable(masseurId: Long, start: Timestamp, end: Timestamp,
                    rt: ReservationTypeEnum) = DB withSession { implicit session =>

    reservations.filter(
      r => r.masseurId === masseurId && r.startTime <= start && r.typeId === ReservationTypeEnum.typeToLong(Unavailable)).firstOption match {
      case Some(leftSide) => reservations.filter(_.id === leftSide.id) map { x => (x.endTime, x.typeId)
      } update((start, ReservationTypeEnum.typeToLong(Regular))) run
      case _ => None
    }

    reservations.filter(
      r => r.masseurId === masseurId && r.endTime <= end && r.typeId === ReservationTypeEnum.typeToLong(Unavailable)).firstOption match {
      case Some(rightSide) => reservations.filter(_.id === rightSide.id) map { x => (x.endTime, x.typeId)
      } update((start, ReservationTypeEnum.typeToLong(Regular))) run
      case _ => None
    }
  }

  def makeReservation(userId: Long, masseurId: Long, start: Timestamp, end: Timestamp) : Option[Long] = DB withTransaction { implicit session =>
    val now = DateTime.now.getMillis

    val reservation = (MassageReservation(None, userId, masseurId, new Timestamp(now), start, end, 0, None, Regular, Chair))
    val pid = Some(((reservations returning reservations.map(_.id)).insert(reservation)))
    pid
  }

  def findAvailableTimes(start: Timestamp, end: Timestamp) = DB withTransaction { implicit session =>

  }

//  def makeReservation(masseurId: Long, userId: Long,)

}
