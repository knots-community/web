package models

import db.TableDefinitions.Masseur
import models.Models._
import models.RoleType._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

/**
 * Created by anton on 9/20/14.
 */
case class MasseurProfile(id: Option[Long], userId: Option[Long], firstName: String, lastName: String, email: String,
                          sex: String, isActive: Boolean = true, massageTypes: Option[List[MassageTypeEnum]])

object Masseurs extends Dao[models.db.TableDefinitions.Masseurs, Masseur] {

  import db.TableDefinitions.DbUser

  tableQuery = masseurs

  implicit def masseurProfile2DbUser(p: MasseurProfile): DbUser = DbUser(p.userId, p.firstName, p.lastName, p.email)
  implicit def masseurProfile2Masseur(p: MasseurProfile): Masseur = Masseur(p.id, p.userId.getOrElse(0), p.sex, p.isActive)

  def getAllMasseurs = DB withSession { implicit session =>
    import db.TableDefinitions.DbUser
    val q: List[(Masseur, DbUser)] = (for {m <- masseurs
                                           u <- users
                                           if (u.id === m.userId)} yield (m, u)).list
    q.map({ case (m, u) => new MasseurProfile(m.id, u.id, u.firstName, u.lastName, u.email, m.sex, m.isActive, Some(MassageTypeEnum.convertFromDbList((for {mmt <- masseurMassageTypes
                                                                                                                                                            if mmt.masseurId === m.id
                                                                                                                                                            mt <- massageTypes} yield mt) list)))
    })
  }

  def addMasseur(p: MasseurProfile) = DB.withSession { implicit session =>
    import db.TableDefinitions.DbUserRole
    p.id match {
      case Some(i) => {
        val m = masseurs.filter(_.id === p.id).first
        val newP = p.copy(id = m.id, userId = Some(m.userId))
        masseurs.filter(_.id === m.id).update(newP)
        users.filter(_.id === m.userId).update(newP)
      }
      case _ => {
        val newId = (users returning users.map(_.id)).insert(p)
        insert(p.copy(userId = Some(newId)))
        userRoles += DbUserRole(None, Some(newId), Some(MasseurRole))
        p.massageTypes map { x: List[MassageTypeEnum] => x.map(y => MassageTypeEnum.convert2Db(y))
        }
      }
    }
  }

  def markActive(p: MasseurProfile) = markIsActive(p, true)
  def markInactive(p: MasseurProfile) = markIsActive(p, false)

  def markIsActive(p: MasseurProfile, isActive: Boolean) = DB.withSession { implicit session =>
    tableQuery.filter(_.id === p.id) map (mp => mp.isActive) update isActive run
  }

  def delete(masseurId: Long) = DB.withSession { implicit session =>
    val m = masseurs.filter(_.id === masseurId).first
    masseurMassageTypes.filter(_.masseurId === m.id).delete
    reservations.filter(_.masseurId === m.id).delete
    tableQuery.filter(_.id === masseurId).delete
    users.filter(_.id === m.userId).delete
  }

  def find(masseurId: Long) = DB.withSession { implicit session =>
      val x = masseurs.filter(_.id === masseurId).first
      val u = users.filter(_.id === x.userId).first
      val roles = Some(MassageTypeEnum.convertFromDbList((for {mmt <- masseurMassageTypes
                                                               if mmt.masseurId === x.id
                                                               mt <- massageTypes} yield mt) list))
     new MasseurProfile(x.id, u.id, u.firstName,u.lastName, u.email, x.sex, x.isActive, roles)
  }
}