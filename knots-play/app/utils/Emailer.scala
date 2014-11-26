package utils


import com.typesafe.plugin.{MailerPlugin, MailerAPI}
import play.api.Play.current


/**
 * Created by anton on 11/26/14.
 */
object Emailer {

  def sendEmail(recipient: String, subject: String, text: String) = {
    val mail: MailerAPI = play.Play.application.plugin(classOf[MailerPlugin]).email
    mail.setSubject(subject)
    mail.setRecipient(recipient)
    mail.setFrom("team@tryknots.com")
    mail.send(text)
  }
}
