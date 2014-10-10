package models

import models.db.TableDefinitions.MassageType
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

/**
 * Created by anton on 9/23/14.
 */
sealed trait MassageTypeEnum
case object Swedish extends MassageTypeEnum
case object HotStone extends MassageTypeEnum
case object Chair extends MassageTypeEnum
case object DeepTissue extends MassageTypeEnum
case object Thai extends MassageTypeEnum
case object Shiatsu extends MassageTypeEnum

object MassageTypeEnum extends Dao {

  implicit def convertFromDb(value: MassageType) : MassageTypeEnum =  value match {
    case MassageType(Some(1), _, _) => Swedish
    case MassageType(Some(2), _, _) => HotStone
    case MassageType(Some(3), _, _) => Chair
    case MassageType(Some(4), _, _) => DeepTissue
    case MassageType(Some(5), _, _) => Thai
    case MassageType(Some(6), _, _) => Shiatsu
  }

  implicit def convert2Db(value: MassageTypeEnum) : MassageType = value match {
    case Swedish => MassageType(Some(1), "Swedish", Some(""))
    case HotStone => MassageType(Some(2), "Hot Stone", Some(""))
    case Chair => MassageType(Some(3), "Chair", Some(""))
    case DeepTissue => MassageType(Some(4), "Deep Tissue", Some(""))
    case Thai => MassageType(Some(5), "Thai", Some(""))
    case Shiatsu => MassageType(Some(6), "Shiatsu", Some(""))
    case _ => MassageType(Some(3), "Chair", Some(""))
  }

  implicit def convert2List(value: MassageTypeEnum): List[MassageTypeEnum] = List(value)
  implicit def convert2Long(value: MassageTypeEnum) : Long = value match {
    case Swedish => 1
    case HotStone => 2
    case Chair => 3
    case DeepTissue => 4
    case Thai => 5
    case Shiatsu => 6
  }

  implicit def convertList2List(value: List[MassageTypeEnum]) : List[MassageType] = value.map(x => convert2Db(x))
  implicit def convertFromDbList(value: List[MassageType]) : List[MassageTypeEnum] = value.map(x => convertFromDb(x))

  def initialize = DB withSession { implicit session =>
      if(massageTypes.list.length ==0) {
        massageTypes += Swedish
        massageTypes += HotStone
        massageTypes += Chair
        massageTypes += DeepTissue
        massageTypes += Thai
        massageTypes += Shiatsu
      }
  }
}
