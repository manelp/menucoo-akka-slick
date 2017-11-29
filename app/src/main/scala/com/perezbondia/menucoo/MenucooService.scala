package com.perezbondia.menucoo

import com.perezbondia.menucoo.calendar.WeekMenu

class MenucooService() {

  def getWeekMenu(yearWeek: String): WeekMenu = {
    WeekMenu(yearWeek, Seq())
  }
}
