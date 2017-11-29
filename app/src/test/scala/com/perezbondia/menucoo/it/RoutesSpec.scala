package com.perezbondia.menucoo.it

//#test-top

import java.time.LocalDate

import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.perezbondia.menucoo._
import com.perezbondia.menucoo.calendar.{DayMenu, WeekMenu}
import com.perezbondia.menucoo.dishes.{Dish, DishesRoutes, DishesService}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

//#set-up
class RoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
  with DishesRoutes with MenucooRoutes {
  //#test-top

  // Here we need to implement all the abstract members of UserRoutes.
  // We use the real UserRegistryActor to test it while we hit the Routes, 
  // but we could "mock" it by implementing it in-place or by using a TestProbe()
  lazy val ctx = new Context()

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = ctx.jsonStreamingSupport

  ctx.migrations.clean()
  ctx.migrations.migrate()

  override val dishesService: DishesService = ctx.dishesService
  override val menucooService: MenucooService = new MenucooService()

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  "DishesRoutes" should {
    "return no dishes if no present (GET /dishes)" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/dishes")

      request ~> dishesRoutes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[Seq[Dish]] should ===(Seq())
      }
    }

    "be able to add users (POST /dishes)" in {

      val dish = Dish(None, "jp")
      val userEntity = Marshal(dish).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      val request = Post("/dishes").withEntity(userEntity)

      request ~> dishesRoutes ~> check {
        status should ===(StatusCodes.Created)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and we know what message we're expecting back:
        entityAs[Dish] should ===(Dish(Some(1), "jp"))
      }
    }
  }

  "MenucooRoutes" should {
    "match week route (GET /weeks/yyyyWww)" in {
      import io.circe.java8.time._

      val testWeek = "2017W01"
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/weeks/" + testWeek)
      val startDay = LocalDate.of(2017, 1, 2)
      val days = (0 to 6).map(n => startDay.plusDays(n)).map(d => DayMenu(d, Seq.empty, Seq.empty))
      val expectedResult = WeekMenu(testWeek, days)

      request ~> menucooRoutes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        //entityAs[WeekMenu] should ===(expectedResult)
      }
    }


  }

}
