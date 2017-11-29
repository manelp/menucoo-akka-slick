package com.perezbondia.menucoo

import akka.actor.ActorSystem
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.util.matching.Regex

trait MenucooRoutes {

  implicit def system: ActorSystem

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.java8.time._
  import io.circe.generic.auto._

  val menucooService: MenucooService

  private val weekRg: Regex = "^(?:[0-9]{4})W(?:(?:0[1-9])|(?:[1-4][0-9])|(?:5[0123]))$".r

  lazy val menucooRoutes: Route =
    pathPrefix("weeks" / weekRg) { (yearWeek) =>
      pathEnd {
        val weekMenu = menucooService.getWeekMenu(yearWeek)
        complete(weekMenu)
      }
    }

}
