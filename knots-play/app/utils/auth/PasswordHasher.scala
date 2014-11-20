package utils.auth

import org.mindrot.jbcrypt.BCrypt

/**
 * Created by anton on 10/8/14.
 */
object PasswordHasher {
  /**
   * Hashes a password.
   *
   * This implementation does not return the salt separately because it is embedded in the hashed password.
   * Other implementations might need to return it so it gets saved in the backing store.
   *
   * @param plainPassword The password to hash.
   * @return A PasswordInfo containing the hashed password.
   */
  def hash(plainPassword: String) = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10))

  /**
   * Checks if a password matches the hashed version.
   *
   * @param hash The password retrieved from the backing store.
   * @param suppliedPassword The password supplied by the user trying to log in.
   * @return True if the password matches, false otherwise.
   */
  def matches(hash: String, suppliedPassword: String) = {
    BCrypt.checkpw(suppliedPassword, hash)
  }
}
