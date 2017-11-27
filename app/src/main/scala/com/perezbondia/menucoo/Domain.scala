package com.perezbondia.menucoo

case class SimpleDish(name: String)

case class Dish(id: Option[Int] = None, name: String) {
  def withId(id: Int): Dish = this.copy(id = Some(id))
}

case class DayMenu(lunch: Seq[Dish], dinner: Seq[Dish])

case class Week(year: Int, week: String, dishes: Seq[DayMenu])
