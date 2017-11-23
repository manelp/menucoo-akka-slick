package com.perezbondia.menucoo

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.server.Directives.{pathEnd, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.util.Timeout

trait DishesRoutes {

  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[DishesRoutes])

  def dishesService: DishesService

  // Required by the `ask` (?) method below
  implicit val timeout: Timeout


  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  lazy val dishesRoutes: Route =
  pathPrefix("dishes") {
      pathEnd {
        get {
          val dishes = dishesService.getDishes
          complete(dishes)
        }
        //#users-get-delete
      }
      //#all-routes
    }

}