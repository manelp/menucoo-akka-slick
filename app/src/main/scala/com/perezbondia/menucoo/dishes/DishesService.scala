package com.perezbondia.menucoo.dishes

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.Source

import scala.concurrent.{ExecutionContext, Future}

class DishesService(dishesRepository: DishesRepository)(implicit ec: ExecutionContext) {

  def getDishes: Source[Dish, NotUsed] = {
    dishesRepository.getDishes
  }

  def getDish(id: UUID): Future[Option[Dish]] = {
    dishesRepository.getDish(id)
  }

  def newDish(simpleDish: SimpleDish): Future[Dish] = {
    dishesRepository.newDish(simpleDish)
  }
}

