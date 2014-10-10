package utils.auth

import org.joda.time.DateTime
import play.api.libs.json.Json

/**
 * Created by anton on 10/7/14.
 */
case class Token(id: String, email: String, pwd: String, expirationDate: DateTime, lastUsedTime: DateTime) {

  def isExpired = expirationDate.isBeforeNow

  def isTimedOut = lastUsedTime.plusMinutes(Token.TokenIdleTimeOut).isBeforeNow

  def isValid = !isExpired && !isTimedOut
}

object Token {
  val TokenIdleTimeOut = 60 * 30; //30 minutes
  val TokenExpiry = 12 * 60 * 60; // 12 hours

  implicit val jsonFormat = Json.format[Token]
}