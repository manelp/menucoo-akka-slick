package com.perezbondia.menucoo

import akka.NotUsed
import akka.stream.scaladsl.Source
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait DishesSchema {

  val jdbcProfile: JdbcProfile

  import jdbcProfile.api._

  class Dishes(tag: Tag) extends Table[Dish](tag, "DISHES") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def name = column[String]("name")
    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id.?, name) <> (Dish.tupled, Dish.unapply)
  }
  val dishes = TableQuery[Dishes]

}

class DishesRepository(dbConfig: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext) extends DishesSchema {
  val jdbcProfile = dbConfig.profile
  import jdbcProfile.api._
  val db = dbConfig.db

  def getDishes: Source[Dish, NotUsed] = {
    Source.fromPublisher(db.stream(dishes.result).mapResult(dr => Dish(dr.id, dr.name)))
  }

  def getDish(id: Int): Future[Option[Dish]] = {
    db.run(dishes.filter(_.id === id).result.headOption).map(_.map(dr => Dish(dr.id, dr.name)))
  }

  def newDish(nd: SimpleDish): Future[Dish] = {
    val insertDish = (dishes returning dishes.map(_.id) into ((d, id) => d.copy(id = Some(id)))) += Dish(name = nd.name)
    db.run(insertDish)
  }

}
