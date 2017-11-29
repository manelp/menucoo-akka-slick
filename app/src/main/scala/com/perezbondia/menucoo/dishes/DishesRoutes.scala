package com.perezbondia.menucoo.dishes

import akka.actor.ActorSystem
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{pathEnd, pathPrefix, _}
import akka.http.scaladsl.server.Route

trait DishesRoutes {

  implicit def system: ActorSystem

  val dishesService: DishesService

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  lazy val dishesRoutes: Route =
    pathPrefix("dishes") {
      pathEnd {
        get {
          val dishes = dishesService.getDishes
          complete(dishes)
        } ~
          post {
            entity(as[SimpleDish]) { d =>
              val dish = dishesService.newDish(d)
              complete(StatusCodes.Created -> dish)
            }
          }
      }
    }

}
