package com.perezbondia.menucoo.it

//#test-top
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import com.perezbondia.menucoo.{Context, DishesRoutes, DishesService}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

//#set-up
class DishesRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
    with DishesRoutes {
  //#test-top

  // Here we need to implement all the abstract members of UserRoutes.
  // We use the real UserRegistryActor to test it while we hit the Routes, 
  // but we could "mock" it by implementing it in-place or by using a TestProbe()
  lazy val routes = dishesRoutes

  lazy val ctx = new Context()

  override def dishesService: DishesService = ctx.dishesService

  override implicit val timeout: Timeout = ctx.timeout

  //#set-up

  //#actual-test
  "DishesRoutes" should {
    "return no users if no present (GET /dishes)" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/dishes")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""[]""")
      }
    }
    //#actual-test

    //#testing-post
//    "be able to add users (POST /users)" in {
//      import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
//      import io.circe.generic.auto._
//
//      val user = User("Kapi", 42, "jp")
//      val userEntity = Marshal(user).to[MessageEntity].futureValue // futureValue is from ScalaFutures
//
//      // using the RequestBuilding DSL:
//      val request = Post("/users").withEntity(userEntity)
//
//      request ~> routes ~> check {
//        status should ===(StatusCodes.Created)
//
//        // we expect the response to be json:
//        contentType should ===(ContentTypes.`application/json`)
//
//        // and we know what message we're expecting back:
//        entityAs[ActionPerformed] should ===(ActionPerformed("User Kapi created."))
//      }
//    }
//    //#testing-post
//
//    "be able to remove users (DELETE /users)" in {
//      // user the RequestBuilding DSL provided by ScalatestRouteSpec:
//      val request = Delete(uri = "/users/Kapi")
//
//      request ~> routes ~> check {
//        status should ===(StatusCodes.OK)
//
//        // we expect the response to be json:
//        contentType should ===(ContentTypes.`application/json`)
//
//        // and no entries should be in the list:
//        entityAs[String] should ===("""{"description":"User Kapi deleted."}""")
//      }
//    }
    //#actual-test
  }
  //#actual-test

  //#set-up

}
//#set-up
