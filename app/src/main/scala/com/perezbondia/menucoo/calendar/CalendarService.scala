package com.perezbondia.menucoo.calendar

import scala.concurrent.Future

class CalendarService(calendarRepository: CalendarRepository) {

  def getWeekMenu(yearWeek: String): Future[Seq[DayMenu]] = {
    val startingDay = localDateFromWeekYear(yearWeek, 1)
    calendarRepository.getWeekMenu(startingDay)
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
