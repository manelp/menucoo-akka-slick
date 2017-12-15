package com.perezbondia.menucoo.calendar

import java.sql.Date
import java.time.LocalDate
import java.util.UUID

import com.perezbondia.menucoo.dishes.Dish.DishId
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext


object MenuType extends Enumeration {
  val lunch = Value
  val dinner = Value
}

case class DayMenuRow(day: LocalDate, menuType: MenuType.Value, dishId: DishId)

object DayMenuRow {
  def fromRequest(day: LocalDate, r: DayMenuRequest): Seq[DayMenuRow] =
    r.lunch.map(d => DayMenuRow(day, MenuType.lunch, d)) ++
      r.dinner.map(d => DayMenuRow(day, MenuType.dinner, d))

}


trait CalendarSchema {
  val jdbcProfile: JdbcProfile

  import jdbcProfile.api._


  class DayMenuTable(tag: Tag) extends Table[DayMenuRow](tag, "day_menus") {
    implicit val localDateToDate = MappedColumnType.base[LocalDate, Date](
      l => Date.valueOf(l), d => d.toLocalDate
    )
    implicit val menuTypeToString = MappedColumnType.base[MenuType.Value, String](
      mt => mt.toString, s => MenuType.withName(s)
    )

    def day = column[LocalDate]("day", O.PrimaryKey)

    def menuType = column[MenuType.Value]("menu_type")

    def dishId = column[UUID]("dish")

    def * = (day, menuType, dishId) <> ((DayMenuRow.apply _).tupled, DayMenuRow.unapply)
  }

  val dayMenus = TableQuery[DayMenuTable]
}


class CalendarRepository(dbConfig: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext) extends CalendarSchema {
  val jdbcProfile = dbConfig.profile

  import jdbcProfile.api._

  val db = dbConfig.db

  def setDayMenu(day: LocalDate, dm: DayMenuRequest): Unit = {
    val insert = DayMenuRow.fromRequest(day, dm).map(r =>
      (dayMenus returning dayMenus).insertOrUpdate(r)
    )
    insert.foreach(db.run)
  }
}
