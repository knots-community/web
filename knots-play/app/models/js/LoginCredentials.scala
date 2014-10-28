package models.js

import java.sql.Timestamp

import org.joda.time.DateTime

/**
 * Created by anton on 10/7/14.
 */
case class LoginCredentials(email: String, password: String)

case class Signup(firstName: String, lastName: String, email: String, password: String, companyName: String)

case class Booking(masseurId: Long, startTime: DateTime)

case class TimeSlotsForCompany(company: String)