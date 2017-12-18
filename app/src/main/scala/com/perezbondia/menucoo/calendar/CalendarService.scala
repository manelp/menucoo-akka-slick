package com.perezbondia.menucoo.calendar

import scala.concurrent.Future

class CalendarService(calendarRepository: CalendarRepository) {

  def getWeekMenu(yearWeek: String): WeekMenu = {
    WeekMenu(yearWeek, Seq())
  }

  def getDayMenu(yearWeek: String, day: Int): Future[DayMenu] = {
    val ld = localDateFromWeekYear(yearWeek, day)
    calendarRepository.getDayMenu(ld)
  }

  def setDayMenu(yearWeek: String, day: Int, menu: DayMenuRequest): Future[DayMenu] = {
    val ld = localDateFromWeekYear(yearWeek, day)
    calendarRepository.setDayMenu(ld, menu)
    getDayMenu(yearWeek, day)
  }
}
