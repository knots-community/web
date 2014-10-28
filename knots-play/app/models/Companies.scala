package models

import java.util.UUID

import models.db.TableDefinitions.Company
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

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

  def findByName(name: String) = DB withSession { implicit session =>
    companies.filter(_.name === name).first
  }

  def update(company: Company) = DB withSession { implicit session =>
    companies.filter(_.id === company.id).update(company)
  }

  def delete(id: Long) = DB withSession { implicit session =>
    companies.filter(_.id === id).delete
  }

  def add(company: Company) = DB withSession { implicit session =>
    val c = company.copy(signupLink = Some(generateSignupLink))
    companies += c
  }

  def initialize = DB withSession { implicit session =>
    val signupLink = Some(generateSignupLink())
    if(companies.length.run == 0) companies += Company(None, "Knots Community", "Montreal", "", "anton@knotsmcgill.com", signupLink)
  }

  def findBySignupLink(link: String) = DB withSession { implicit session =>
    companies.filter(_.signupLink === link).first
  }

  private def generateSignupLink() = {
    UUID.randomUUID().toString
  }
}
