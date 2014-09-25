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
case class MasseurProfile(id: Option[Long], userId: Option[Long], firstName: Option[String],
                     lastName: Option[String], email: Option[String], sex: String, isActive: Boolean = false,
                             massageTypes: Option[List[MassageTypeEnum]])

object Masseurs extends Dao[models.db.TableDefinitions.Masseurs, Masseur] {

  import db.TableDefinitions.DbUser

  tableQuery = masseurs

  implicit def masseurProfile2DbUser(p : MasseurProfile) : DbUser = DbUser(p.userId, p.firstName, p.lastName, p.email)
  implicit def masseurProfile2Masseur(p: MasseurProfile) : Masseur = Masseur(p.id, p.userId.getOrElse(0), p.sex, p.isActive)

  def getAllMasseurs = DB withSession { implicit session =>
    import db.TableDefinitions.{MassageType, DbUser}
    val q: List[(Masseur, DbUser)] = (for {m <- masseurs
                                           u <- users
                                           } yield (m, u)).list

    import MassageTypeEnum._
    q.map({ case (m, u) => new MasseurProfile(m.id, u.id, u.firstName, u.lastName, u.email, m.sex, m.isActive,
      Some(MassageTypeEnum.convertFromDbList((for {mmt <- masseurMassageTypes
        if mmt.masseurId == m.id
          mt <- massageTypes
        } yield mt) list))
    )})
  }

  def addMasseur(p: MasseurProfile) = DB.withSession { implicit session =>
    import db.TableDefinitions.DbUserRole
    val newId = (users returning users.map(_.id)).insert(p)
    insert(p.copy(userId = Some(newId)))
    userRoles += DbUserRole(None, Some(newId), Some(MasseurRole))
    p.massageTypes map {
      x: List[MassageTypeEnum] => x.map(y => MassageTypeEnum.convert2Db(y))
    }
  }

  def markActive(p: MasseurProfile) = markIsActive(p, true)
  def markInactive(p: MasseurProfile) = markIsActive(p, false)

  def markIsActive(p: MasseurProfile, isActive: Boolean) = DB.withSession { implicit  session =>
    tableQuery.filter(_.id === p.id) map (mp => mp.isActive) update isActive run
  }

}
