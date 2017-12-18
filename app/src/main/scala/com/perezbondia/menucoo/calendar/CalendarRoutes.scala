package com.perezbondia.menucoo.calendar

import akka.actor.ActorSystem
import akka.http.scaladsl.common.JsonEntityStreamingSupport
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.util.matching.Regex

trait CalendarRoutes {

  implicit def system: ActorSystem

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.java8.time._
  import io.circe.generic.auto._

  val calendarService: CalendarService

  private val weekRg: Regex = "^(?:[0-9]{4})-W(?:(?:0[1-9])|(?:[1-4][0-9])|(?:5[0123]))$".r

  lazy val calendarRoutes: Route =
    pathPrefix("weeks" / weekRg) { (yearWeek) =>
      pathEnd {
        val weekMenu = calendarService.getWeekMenu(yearWeek)
        complete(weekMenu)
      } ~
        pathPrefix("days" / IntNumber) { day =>
          get {
            rejectEmptyResponse {
              val dayMenu = calendarService.getDayMenu(yearWeek, day)
              complete(dayMenu)
            }
          } ~
          post {
            entity(as[DayMenuRequest]) { dayMenu =>
              val menu = calendarService.setDayMenu(yearWeek, day, dayMenu)
              complete(StatusCodes.Created -> menu)
            }
          }
        }

    }

}
