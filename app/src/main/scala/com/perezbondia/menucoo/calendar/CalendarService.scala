package com.perezbondia.menucoo.calendar

import java.time.LocalDate

class CalendarService(calendarRepository: CalendarRepository) {

  def getWeekMenu(yearWeek: String): WeekMenu = {
    WeekMenu(yearWeek, Seq())
  }

  def getDayMenu(yearWeek: String, day: Int): Option[DayMenu] = {
    Some(DayMenu(LocalDate.now(), Seq.empty, Seq.empty))
  }

  def setDayMenu(yearWeek: String, day: Int, menu: DayMenuRequest): DayMenu = {
    calendarRepository.setDayMenu(LocalDate.now, menu)
    val dayMenu = DayMenu(LocalDate.now, Seq.empty, Seq.empty)
    dayMenu
  }
}
