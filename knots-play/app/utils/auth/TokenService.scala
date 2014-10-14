package utils.auth

import java.util.UUID

import com.atlassian.jwt.SigningAlgorithm
import com.atlassian.jwt.core.writer.{JsonSmartJwtJsonBuilder, NimbusJwtWriterFactory}
import com.mohiva.play.silhouette.core.exceptions.AuthenticationException
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.JWTClaimsSet
import org.joda.time.DateTime
import play.api.Play._
import play.api.libs.Crypto
import play.api.libs.json.Json

import scala.util.Try

/**
 * Created by anton on 10/7/14.
 */
object TokenService {
  val Secret = current.configuration.getString("application.secret").get
  val Verifier = new MACVerifier(Secret)

  def create(email: String) : Token = {
    val genId = UUID.randomUUID().toString
    val now = DateTime.now
    Token(genId, email, now, now.plusSeconds(Token.TokenExpiry))
  }

  def serialize(token: Token) = {
    val subject = Json.toJson(token).toString()
    val jwtBuilder = new JsonSmartJwtJsonBuilder()
      .jwtId(token.id)
      .issuer(Token.Issuer)
      .subject(Crypto.encryptAES(subject))
      .issuedAt(token.lastUsedTime.getMillis / 1000)
      .expirationTime(token.expirationDate.getMillis / 1000)

    new NimbusJwtWriterFactory()
      .macSigningWriter(SigningAlgorithm.HS256, Secret)
      .jsonToJwt(jwtBuilder.build())
  }

  def deserialize(str: String) : Option[Token] = {
    try {
      val jwsObject = JWSObject.parse(str)
      if(!jwsObject.verify(Verifier)) throw new IllegalArgumentException("Fraudulent JWT token: " + str)

      val claimSet = JWTClaimsSet.parse(jwsObject.getPayload.toJSONObject)
      val subject = Crypto.decryptAES(claimSet.getSubject)
      Json.parse(subject).validate[Token] map (token => Some(token)) getOrElse(throw new AuthenticationException(""))
    } catch {
      case _ => None
    }
  }

  def renew(token: Token) = {
    create(token.email)
  }
}