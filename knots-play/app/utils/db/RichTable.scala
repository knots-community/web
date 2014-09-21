package utils.db
import play.api.db.slick.Config.driver.simple._

/**
 * Created by anton on 9/20/14.
 */
abstract class RichTable[T](tag: Tag, name: String) extends Table[T](tag, name) {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
}