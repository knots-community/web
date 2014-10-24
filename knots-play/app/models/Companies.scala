package models

import models.db.TableDefinitions.{Company}
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

import scala.concurrent.Future

/**
 * Created by anton on 10/23/14.
 */
object CompaniesDao extends Dao{

  def findAll = DB withSession { implicit session =>
    companies.list
  }

  def findById(id: Long) = DB withSession { implicit session =>
    companies.filter(_.id === id).first
  }

  def update(company: Company) = DB withSession { implicit session =>
    companies.filter(_.id === company.id).update(company)
  }

  def delete(id: Long) = DB withSession { implicit session =>
    companies.filter(_.id === id).delete
  }

  def add(company: Company) = DB withSession { implicit request =>
    companies += company
  }

  def initialize = DB withSession { implicit request =>
//    companies.insertOrUpdate(Company(None, "Knots Community", "Montreal", "", "anton@knotsmcgill.com"))
  }

}
