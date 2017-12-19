package com.perezbondia.menucoo.calendar

import java.time.LocalDate

import akka.actor.ActorSystem

import scala.concurrent.{ExecutionContext, Future}

class CalendarService(calendarRepository: CalendarRepository) {

  implicit val system: ActorSystem = ActorSystem("menucooAkka")
  implicit val executionContext: ExecutionContext = system.dispatcher

  def getWeekMenu(yearWeek: String): Future[WeekMenu] = {
    val startingDay = localDateFromWeekYear(yearWeek, 1)
    calendarRepository.getWeekMenu(startingDay).map(menus =>
      WeekMenu(yearWeek, menus)
    )
  }

  def getDayMenu(day: LocalDate): Future[DayMenu] = {
    calendarRepository.getDayMenu(day)
  }

  def setDayMenu(day: LocalDate, menu: DayMenuRequest): Future[DayMenu] = {
    calendarRepository.setDayMenu(day, menu)
    getDayMenu(day)
  }
}
