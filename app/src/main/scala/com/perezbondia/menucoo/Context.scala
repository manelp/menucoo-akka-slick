package com.perezbondia.menucoo

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.postgresql.ds.PGSimpleDataSource
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

class Context extends DishesRoutes {

  lazy private val config: Config = ConfigFactory.load()
  // set up ActorSystem and other dependencies here
  //#main-class
  //#server-bootstrapping
  implicit val system: ActorSystem = ActorSystem("menucooAkka")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  //#server-bootstrapping

  // Needed for the Future and its methods flatMap/onComplete in the end
  implicit val executionContext: ExecutionContext = system.dispatcher

  //#http-server
  private val httpConfig: Config = config.getConfig("http")

  private val interface = httpConfig.getString("interface")
  private val port = httpConfig.getInt("port")
  override implicit val timeout: Timeout = Timeout(httpConfig.getInt("timeout").seconds)

  // Database
  lazy val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig[JdbcProfile]("slick")

  lazy val dishesRepository = new DishesRepository(dbConfig)

  override def dishesService = new DishesService(dishesRepository)

  lazy val log = Logging(system, classOf[Context])

  val migrations = {
    val flyway = new Flyway()
    val dataSource = new PGSimpleDataSource()
    dataSource.setUrl(dbConfig.config.getString("db.url"))
    dataSource.setUser(dbConfig.config.getString("db.user"))
    dataSource.setPassword(dbConfig.config.getString("db.password"))
    flyway.setDataSource(dataSource)
    flyway
  }

  def start(dockerized: Boolean = true): Unit = {
    val serverBindingFuture: Future[ServerBinding] = Http().bindAndHandle(dishesRoutes, interface, port)

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
