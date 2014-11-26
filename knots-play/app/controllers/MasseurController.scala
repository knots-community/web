package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Silhouette, Environment}
import models.{MasseurProfile, Masseurs, AdminUser}
import play.api.data.Form
import play.api.data.Forms._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

/**
 * Created by anton on 11/21/14.
 */
class MasseurController @Inject()(implicit val env: Environment[AdminUser, CachedCookieAuthenticator])
  extends Silhouette[AdminUser, CachedCookieAuthenticator] {

  val addMasseurForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "userId" -> optional(longNumber),
      "firstName" -> nonEmptyText(1),
      "lastName" -> nonEmptyText(1),
      "email" -> email,
      "sex" -> nonEmptyText()
    )((id, userId, firstName, lastName, email, sex) => {
      MasseurProfile(id, userId, firstName, lastName, email, sex)
    })
      ((mp: MasseurProfile) => Some(mp.id, mp.userId, mp.firstName, mp.lastName, mp.email, mp.sex))
  )


  def listMasseurs = UserAwareAction.async { implicit request =>
    val futureMasseurs = Future {
      Masseurs.getAllMasseurs
    }
    futureMasseurs.map { masseurList => Ok(views.html.admin.masseur.list(request.identity, masseurList))}
  }

  def addMasseur = SecuredAction.async { implicit request =>
    val form = if (request.flash.get("error").isDefined) addMasseurForm.bind(request.flash.data) else addMasseurForm
    Future.successful(Ok(views.html.admin.masseur.add(Some(request.identity), form)))
  }

  def saveMasseur = SecuredAction { implicit request =>
    val masseurForm = addMasseurForm.bindFromRequest()
    if (masseurForm.hasErrors) {
      import play.Logger
      Logger.error(masseurForm.data.toString());
      Logger.error(masseurForm.errors.toString());
    }
    masseurForm.fold(
      hasErrors = { form =>
        import play.api.mvc.Flash
        Redirect(routes.MasseurController.addMasseur()).flashing(Flash(form.data))
      },
      success = { newMasseur =>
        Masseurs.addMasseur(newMasseur)
        Redirect(routes.MasseurController.listMasseurs())
      }
    )
  }

  def updateMasseur(id: Long) = SecuredAction { implicit request =>
    addMasseurForm.bindFromRequest().fold(
      hasErrors = { form => import play.api.mvc.Flash
        Redirect(routes.MasseurController.editMasseur(id)).flashing(Flash(form.data))
      },
      success = { updatedMasseur =>
        Masseurs.addMasseur(updatedMasseur.copy(id = Some(id)))
        Redirect(routes.MasseurController.listMasseurs())
      }
    )
  }

  def editMasseur(masseurId: Long) = SecuredAction { implicit request =>
    val m: MasseurProfile = Masseurs.find(masseurId)
    val form = addMasseurForm.fill(m)
    Ok(views.html.admin.masseur.edit(Some(request.identity), form, masseurId))
  }

  def deleteMasseur(masseurId: Long) = SecuredAction { implicit request =>
    Masseurs.delete(masseurId)
    Redirect(routes.MasseurController.listMasseurs())
  }

  def showMasseur(masseurId: Long) = SecuredAction { implicit request =>
    val masseur = Masseurs.find(masseurId)
    Ok(views.html.admin.masseur.view(Some(request.identity), masseur))
  }

}
