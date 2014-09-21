package models.services

import com.mohiva.play.silhouette.core.LoginInfo
import com.mohiva.play.silhouette.core.providers.CommonSocialProfile
import com.mohiva.play.silhouette.core.services.{AuthInfo, IdentityService}
import models.auth.User
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class UserService extends IdentityService[User]{

  import models.auth.Users

  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = Users.find(loginInfo)

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User) = Users.save(user)

  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   *
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  def save[A <: AuthInfo](profile: CommonSocialProfile[A]) = {
    Users.find(profile.loginInfo).flatMap {
      case Some(user) => // Update user with profile
        Users.save(user.copy(
          firstName = profile.firstName,
          lastName = profile.lastName,
          email = profile.email
        ))
      case None => // Insert a new user
        import models.db.TableDefinitions.DbRole
        Users.save(User(
          id = None,
          loginInfo = profile.loginInfo,
          firstName = profile.firstName,
          lastName = profile.lastName,
          email = profile.email,
          List(DbRole(Some(0), "regular", Some("")))
        ))
    }
  }
}
