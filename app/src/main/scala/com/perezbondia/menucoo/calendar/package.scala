package com.perezbondia.menucoo

import java.time.LocalDate
import java.time.format.DateTimeFormatter

package object calendar {

  def localDateFromWeekYear(yearWeek: String, day: Int): LocalDate = {
    LocalDate.parse(s"$yearWeek-1", DateTimeFormatter.ISO_WEEK_DATE).plusDays(day - 1)
  }
}
