package models

import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import utils.db.RichTable

/**
 * Created by anton on 9/18/14.
 */
trait Dao[T <: RichTable[A], A] {

  var tableQuery: TableQuery[T] = _

  /*
    * Find a specific entity by id.
    */
  def findById(id: Long)(implicit session: Session): Option[A] = {
    DB withSession { implicit session =>
      tableQuery.filter(_.id === id).firstOption
    }
  }

  /**
   * Delete a specific entity by id. If successfully completed return true, else false
   */
  def delete(id: Long)(implicit session: Session) = {
    DB withSession { implicit session =>

      tableQuery.filter(_.id === id).delete > 0
    }
  }

  /**
   * Update a specific entity by id. If successfully completed return true, else false
   */
  def update(id: Int, entity: A) = {
    DB withSession { implicit session =>
      tableQuery.update(entity) > 0
    }
  }

  def insert(entity: A) = {
    DB withSession { implicit session =>
      tableQuery += entity
    }
  }

  def getAll() = {
    DB withSession { implicit session =>
      tableQuery.list
    }
  }

}