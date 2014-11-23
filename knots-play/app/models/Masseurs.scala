package models

import models.RoleType._
import models.db.TableDefinitions.{Masseur, User}
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

/**
 * Created by anton on 9/20/14.
 */
case class MasseurProfile(id: Option[Long], userId: Option[Long], firstName: String, lastName: String, email: String,
                          sex: String, isActive: Boolean = true)

object Masseurs extends Dao {

  implicit def masseurProfile2DbUser(p: MasseurProfile): User = User(p.userId, p.firstName, p.lastName, p.email, 1)
  implicit def masseurProfile2Masseur(p: MasseurProfile): Masseur = Masseur(p.id, p.userId.getOrElse(0), p.sex, p.isActive)

  def getAllMasseurs = DB withSession { implicit session =>
    val q: List[(Masseur, User)] = (for {m <- masseurs
                                           u <- users
                                           if u.id === m.userId} yield (m, u)).list
    q.map({ case (m, u) => new MasseurProfile(m.id, u.id, u.firstName, u.lastName, u.email, m.sex, m.isActive)
    })
  }

  def addMasseur(p: MasseurProfile) = DB.withSession { implicit session =>
    import models.db.TableDefinitions.DbUserRole
    p.id match {
      case Some(i) => {
        val m = masseurs.filter(_.id === p.id).first
        val newP = p.copy(id = m.id, userId = Some(m.userId))
        masseurs.filter(_.id === m.id).update(newP)
        users.filter(_.id === m.userId).update(newP)
      }
      case _ => {
        val newId = (users returning users.map(_.id)).insert(p)
        masseurs += p.copy(userId = Some(newId))
        userRoles += DbUserRole(None, Some(newId), Some(MasseurRole))
      }
    }
  }

  def markActive(p: MasseurProfile) = markIsActive(p, true)
  def markInactive(p: MasseurProfile) = markIsActive(p, false)

  def markIsActive(p: MasseurProfile, isActive: Boolean) = DB.withSession { implicit session =>
    masseurs.filter(_.id === p.id) map (mp => mp.isActive) update isActive run
  }

  def delete(masseurId: Long) = DB.withSession { implicit session =>
    val m = masseurs.filter(_.id === masseurId).first
    masseurMassageTypes.filter(_.masseurId === m.id).delete
    reservations.filter(_.masseurId === m.id).delete
    masseurs.filter(_.id === masseurId).delete
    users.filter(_.id === m.userId).delete > 0
  }

  def find(masseurId: Long) = DB.withSession { implicit session =>
      val x = masseurs.filter(_.id === masseurId).first
      val u = users.filter(_.id === x.userId).first
     new MasseurProfile(x.id, u.id, u.firstName,u.lastName, u.email, x.sex, x.isActive)
  }
}