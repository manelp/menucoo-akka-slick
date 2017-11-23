package com.perezbondia.menucoo

import akka.NotUsed
import akka.stream.scaladsl.Source
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class DishesRepository(dbConfig: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext) {
  import datamodel.latest.schema.tables.Dishes
  import dbConfig.profile.api._
  val db = dbConfig.db

  val dishes = TableQuery[Dishes]

  def getDishes: Source[Dish, NotUsed] = {
    Source.fromPublisher(db.stream(dishes.result).mapResult(dr => Dish(Option(dr.id), dr.name)))
  }

  def getDish(id:Int): Future[Option[Dish]] = {
    db.run(dishes.filter(_.id === id).result.headOption).map(_.map(dr => Dish(Option(dr.id), dr.name)))
  }

}
