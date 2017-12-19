package com.perezbondia.menucoo.calendar

import java.time.LocalDate

import akka.actor.ActorSystem
import akka.http.scaladsl.common.JsonEntityStreamingSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{post, _}
import akka.http.scaladsl.server.{Directives, Route}

import scala.util.matching.Regex

trait CalendarRoutes {

  implicit def system: ActorSystem

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.java8.time._
  import io.circe.generic.auto._

  val calendarService: CalendarService

  private val weekRg: Regex = "^(?:[0-9]{4})-W(?:(?:0[1-9])|(?:[1-4][0-9])|(?:5[0123]))$".r
  private val dayRg: Regex = "^[0-9]{4}-(?:[0][0-9]|1[0-2])-(?:[0-2][0-9]|3[01])$".r

  lazy val calendarRoutes: Route =
    pathPrefix("calendar") {
      pathPrefix("views") {
        pathPrefix("week" / weekRg) { (yearWeek) =>
          pathEnd {
            val weekMenu = calendarService.getWeekMenu(yearWeek)
            complete(weekMenu)
          }
        } ~
          pathPrefix("day" / dayRg) { (dayStr) =>
            val day = LocalDate.parse(dayStr)
            get {
              rejectEmptyResponse {
                val dayMenu = calendarService.getDayMenu(day)
                complete(dayMenu)
              }
            } ~
              post {
                entity(as[DayMenuRequest]) { dayMenu =>
                  val menu = calendarService.setDayMenu(day, dayMenu)
                  complete(StatusCodes.Created -> menu)
                }
              }
          }
      }
    }

}
