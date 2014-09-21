package models

import db.TableDefinitions.Masseur
import models.Models._

/**
 * Created by anton on 9/20/14.
 */
object Masseurs extends Dao[models.db.TableDefinitions.Masseurs, Masseur] {
  tableQuery = masseurs
}
