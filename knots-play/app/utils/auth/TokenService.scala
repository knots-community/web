package utils.auth

import java.util.UUID

import com.nimbusds.jose.{Payload, JWSAlgorithm, JWSHeader, JWSObject}
import com.nimbusds.jose.crypto.{MACVerifier, MACSigner}
import org.joda.time.DateTime
import play.api.Play._
import play.api.libs.json.Json

import scala.util.{Success, Try, Failure}

/**
 * Created by anton on 10/7/14.
 */
object TokenService {
  val Secret = current.configuration.getString("application.secret").get
  val Signer = new MACSigner(Secret)

  def create(userEmail: String, userPwd: String) = {
    val genId = UUID.randomUUID().toString
    val now = DateTime.now
    Token(id = genId, email = userEmail, pwd = userPwd, expirationDate = now.plusSeconds(Token.TokenExpiry), lastUsedTime = now)
  }

  def serialize(token: Token) = {
    val jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(Json.toJson(token).toString()))
    jwsObject.sign(TokenService.Signer)
    jwsObject.toString
  }

  def validate(tokenString: String): Boolean = {
    val jwsObject = JWSObject.parse(tokenString)
    val verifier = new MACVerifier(TokenService.Secret)
    if (jwsObject.verify(verifier)) {
      Try(Json.parse(jwsObject.toString)) match {
        case Success(json) => json.validate[Token].asEither match {
          case Left(error) => false
          case Right(info) => info.isValid
        }
        case Failure(error) => false
      }
    } else {
      false
    }
  }
}

