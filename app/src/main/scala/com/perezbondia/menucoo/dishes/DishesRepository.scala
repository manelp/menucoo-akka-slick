package com.perezbondia.menucoo.dishes

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.Source
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait DishesSchema {

  val jdbcProfile: JdbcProfile

  import jdbcProfile.api._

  class Dishes(tag: Tag) extends Table[Dish](tag, "dishes") {
    def id = column[UUID]("id", O.PrimaryKey) // This is the primary key column
    def name = column[String]("name")
    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, name) <> ((Dish.apply _).tupled, Dish.unapply)
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

  def getDish(id: UUID): Future[Option[Dish]] = {
    db.run(dishes.filter(_.id === id).result.headOption).map(_.map(dr => Dish(dr.id, dr.name)))
  }

  def newDish(nd: SimpleDish): Future[Dish] = {
    val dish = Dish(UUID.randomUUID(), nd.name)
    val insertDish = (dishes returning dishes) += dish
    db.run(insertDish)
  }

}
