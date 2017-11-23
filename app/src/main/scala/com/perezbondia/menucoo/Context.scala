package com.perezbondia.menucoo

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.{ Config, ConfigFactory }
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
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

  def start(): Unit = {
    MyMigrations.migrate()
    val serverBindingFuture: Future[ServerBinding] = Http().bindAndHandle(dishesRoutes, interface, port)

    println(s"Server online at http://$interface:$port/\nPress RETURN to stop...")

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
