package models.js

import java.sql.Timestamp

/**
 * Created by anton on 10/7/14.
 */
case class LoginCredentials(email: String, password: String)

case class Signup(firstName: String, lastName: String, email: String, password: String)

case class Booking(masseurId: Long, startTime: Timestamp)