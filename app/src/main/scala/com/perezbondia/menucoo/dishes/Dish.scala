package com.perezbondia.menucoo.dishes

case class SimpleDish(name: String)

case class Dish(id: Option[Int] = None, name: String) {
  def withId(id: Int): Dish = this.copy(id = Some(id))
}

