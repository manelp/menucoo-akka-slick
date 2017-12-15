package com.perezbondia.menucoo.dishes

import java.util.UUID

case class SimpleDish(name: String)

case class Dish(id: UUID, name: String)

object Dish {
  type DishId = UUID
}


