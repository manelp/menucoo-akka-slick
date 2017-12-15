package com.perezbondia.menucoo.calendar

import java.time.LocalDate

import com.perezbondia.menucoo.dishes.Dish
import com.perezbondia.menucoo.dishes.Dish.DishId

case class DayMenu(day: LocalDate, lunch: Seq[Dish], dinner: Seq[Dish])

case class DayMenuRequest(lunch: Seq[DishId], dinner: Seq[DishId])

case class WeekMenu(yearWeek: String, days: Seq[DayMenu])
