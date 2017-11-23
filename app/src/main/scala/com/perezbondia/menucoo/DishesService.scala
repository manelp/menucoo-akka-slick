package com.perezbondia.menucoo

import akka.NotUsed
import akka.stream.scaladsl.Source

import scala.concurrent.Future


class DishesService(dishesRepository: DishesRepository) {

  def getDishes: Source[Dish, NotUsed] = {
    dishesRepository.getDishes
  }

  def getDish(id: Int): Future[Option[Dish]] = {
    dishesRepository.getDish(id)
  }
}

