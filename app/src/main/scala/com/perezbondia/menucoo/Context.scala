package com.perezbondia.menucoo

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.common.{ EntityStreamingSupport, JsonEntityStreamingSupport }
import akka.stream.ActorMaterializer
import com.perezbondia.menucoo.dishes.{ DishesRepository, DishesRoutes, DishesService }
import com.typesafe.config.{ Config, ConfigFactory }
import org.flywaydb.core.Flyway
import org.postgresql.ds.PGSimpleDataSource
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }
import scala.io.StdIn

class Context extends DishesRoutes with MenucooRoutes with DatabaseUriConfig {

  lazy private val config: Config = ConfigFactory.load()
  // set up ActorSystem and other dependencies here
  //#main-class
  //#server-bootstrapping
  implicit val system: ActorSystem = ActorSystem("menucooAkka")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  //#server-bootstrapping

  // Needed for the Future and its methods flatMap/onComplete in the end
  implicit val executionContext: ExecutionContext = system.dispatcher
  override implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()

  //#http-server
  private val httpConfig: Config = config.getConfig("http")

  private val interface = httpConfig.getString("interface")
  private val port = httpConfig.getInt("port")

  // Database
  lazy val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig[JdbcProfile]("slick")
  override val defaultDbUri = dbConfig.config.getString("db.properties.url")

  lazy val dishesRepository = new DishesRepository(dbConfig)

  override val dishesService = new DishesService(dishesRepository)
  override val menucooService = new MenucooService()

  lazy val log = Logging(system, classOf[Context])

  val migrations = {
    val flyway = new Flyway()
    val dataSource = new PGSimpleDataSource()
    dataSource.setUrl(databaseConnectionUrl)
    dataSource.setUser(databaseUsername)
    dataSource.setPassword(databasePassword)
    flyway.setDataSource(dataSource)
    flyway
  }

  import akka.http.scaladsl.server.Directives._enhanceRouteWithConcatenation

  def start(dockerized: Boolean = true): Unit = {
    val serverBindingFuture: Future[ServerBinding] = Http().bindAndHandle(dishesRoutes ~ menucooRoutes, interface, port)

    println(s"Server online at http://$interface:$port/")

    if (!dockerized) {
      StdIn.readLine()
      serverBindingFuture
        .flatMap(_.unbind())
        .onComplete { done =>
          done.failed.map { ex => log.error(ex, "Failed unbinding") }
          system.terminate()
          dbConfig.db.close()
        }
    }
  }
}
