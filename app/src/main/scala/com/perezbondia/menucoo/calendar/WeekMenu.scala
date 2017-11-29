package com.perezbondia.menucoo.calendar

import java.time.LocalDate

import com.perezbondia.menucoo.dishes.Dish

case class DayMenu(day: LocalDate, lunch: Seq[Dish], dinner: Seq[Dish])

case class WeekMenu(yearWeek: String, days: Seq[DayMenu])
