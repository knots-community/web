package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Environment, Silhouette}
import models.Reservations.ScheduleEntry
import models._
import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Action

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
 * Created by anton on 9/21/14.
 */
class AdminController @Inject()(implicit val env: Environment[AdminUser, CachedCookieAuthenticator])
  extends Silhouette[AdminUser, CachedCookieAuthenticator] {

  /**
   * A play framework form.
   */
  val addMasseurForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "userId" -> optional(longNumber),
      "firstName" -> nonEmptyText(1),
      "lastName" -> nonEmptyText(1),
      "email" -> email,
      "sex" -> nonEmptyText()
      //       "chair" -> boolean,
      //      "swedish" -> boolean,
      //      "shiatsu" -> boolean
    )((id, userId, firstName, lastName, email, sex) => {
      MasseurProfile(id, userId, firstName, lastName, email, sex)
    })
      ((mp: MasseurProfile) => Some(mp.id, mp.userId, mp.firstName, mp.lastName, mp.email, mp.sex))
  )
  implicit val scheduleFormat = Json.format[ScheduleEntry]
  implicit val masseurOrderJson = Json.format[MasseurOrder]
  implicit val masseurFormat = Json.format[MasseurProfile]

  /**
   * Handles the Sign Up action.
   *
   * @return The result to display.
   */
  def signup = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => import concurrent.Future
        Future.successful(Redirect(routes.AdminController.index()))
      case None => import forms.SignUpForm
        Future.successful(Ok(views.html.admin.signup(SignUpForm.form)))
    }
  }

  def signin = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) =>
        Future.successful(Redirect(routes.AdminController.index))
      case None => import forms.SignInForm
        Future.successful(Ok(views.html.admin.signin(SignInForm.form)))
    }
  }

  def index = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) =>
        Future.successful(Redirect(routes.AdminController.listUsers()))
      case None => import forms.SignInForm
        Future.successful(Ok(views.html.admin.signin(SignInForm.form)))
    }
  }

  def listUsers = SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.admin.users.list(Some(request.identity), Users.getAll)))
  }

  def listMasseurs = SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.admin.masseur.list(Some(request.identity), Masseurs.getAllMasseurs)))
  }

  def addMasseur = SecuredAction.async { implicit request =>
    val form = if (request.flash.get("error").isDefined) addMasseurForm.bind(request.flash.data) else addMasseurForm
    Future.successful(Ok(views.html.admin.masseur.add(Some(request.identity), form)))
  }

  def saveMasseur = SecuredAction.async { implicit request =>
    val masseurForm = addMasseurForm.bindFromRequest()
    if (masseurForm.hasErrors) {
      import play.Logger
      Logger.error(masseurForm.data.toString())
      Logger.error(masseurForm.errors.toString())
    }
    masseurForm.fold(
      hasErrors = { form =>
        import play.api.mvc.Flash
        Future.successful(Redirect(routes.AdminController.addMasseur()).flashing(Flash(form.data)))
      },
      success = { newMasseur =>
        Future.successful {
          Masseurs.addMasseur(newMasseur)
          Redirect(routes.AdminController.listMasseurs())
        }
      }
    )
  }

  def updateMasseur(id: Long) = SecuredAction.async { implicit request =>
    addMasseurForm.bindFromRequest().fold(
      hasErrors = { form => import play.api.mvc.Flash
        Future.successful(Redirect(routes.AdminController.editMasseur(id)).flashing(Flash(form.data)))
      },
      success = { updatedMasseur =>
        Future.successful {
          Masseurs.addMasseur(updatedMasseur.copy(id = Some(id)))
          Redirect(routes.AdminController.listMasseurs())
        }
      }
    )
  }

  def editMasseur(masseurId: Long) = SecuredAction.async { implicit request =>
    Future.successful {
      val m: MasseurProfile = Masseurs.find(masseurId)
      val form = addMasseurForm.fill(m)
      Ok(views.html.admin.masseur.edit(Some(request.identity), form, masseurId))
    }
  }

  def deleteMasseur(masseurId: Long) = SecuredAction.async { implicit request =>
    Future.successful {
      Masseurs.delete(masseurId)
      Redirect(routes.AdminController.listMasseurs())
    }
  }

  def showMasseur(masseurId: Long) = SecuredAction.async { implicit request =>
    Future.successful {
      val masseur = Masseurs.find(masseurId)
      Ok(views.html.admin.masseur.view(Some(request.identity), masseur))
    }
  }

  def calendar = SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.admin.calendar(Some(request.identity))))
  }

  def schedule = Action { implicit request =>
    val start = request.queryString.get("start").flatMap(_.headOption) getOrElse "invalid"
    val end = request.queryString.get("end").flatMap(_.headOption) getOrElse "invalid"
    val startDate = DateTime.parse(start)
    val endDate = DateTime.parse(end)
    val slots = Reservations.findSchedule(startDate, endDate)
    val res = Json.toJson(slots)
    Ok(res).as(JSON)
  }

  def markMasseurAvailable = Action.async(parse.json) { implicit request =>
    request.body.validate[MasseurOrder].fold(
      errors => Future.successful(BadRequest(Json.obj("status" -> "fail", "message" -> JsError.toFlatJson(errors)))), //return type requires Future[Result]
      mo => {
        Future.successful {
          val start = DateTime.parse(mo.date).withTime(8, 0, 0, 0)
          val end = start.withTime(23, 0, 0, 0)
          Reservations.generateTimeSlots(start, end, mo.masseurId, 0)
          Ok(Json.obj("status" -> "OK")).as(JSON)
        }
      }
    )
  }

  def markMasseurUnvailable = Action.async(parse.json) { implicit request =>
    request.body.validate[MasseurOrder].fold(
      errors => Future.successful(BadRequest(Json.obj("status" -> "fail", "message" -> JsError.toFlatJson(errors)))), //same here
      mo => {
        Future.successful {
          val start = DateTime.parse(mo.date).withTime(8, 0, 0, 0)
          val end = start.withTime(23, 0, 0, 0)
          Reservations.removeTimeSlots(start, end, mo.masseurId)
          Ok(Json.obj("status" -> "OK")).as(JSON)
        }
      }
    )
  }

  def listMasseursJson = SecuredAction.async { implicit request =>
    Future.successful(Json.toJson(Masseurs.getAllMasseurs)).map {
      result => Ok(Json.obj("status" -> "OK", "masseurs" -> result))
    }
  }

  case class MasseurOrder(date: String, masseurId: Long)
  //    val slots = Reservations.findTimeSlots(start, end)
  //    val slots2 = Reservations.findSchedule(start, end)

}
