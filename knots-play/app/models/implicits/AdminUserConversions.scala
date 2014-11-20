package models.implicits

import models.AdminUser
import models.db.TableDefinitions.User

/**
 * Created by anton on 11/16/14.
 */
object AdminUserConversions {
  implicit def admin2User(admin: AdminUser): User = {
    return User(admin.id, admin.firstName, admin.lastName, admin.email, 1)
  }
}
