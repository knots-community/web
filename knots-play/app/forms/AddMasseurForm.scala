package forms

import models.MasseurProfile
import play.api.data.Form
import play.api.data.Forms._

/**
 * Created by anton on 11/21/14.
 */
object AddMasseurForm {
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
}
