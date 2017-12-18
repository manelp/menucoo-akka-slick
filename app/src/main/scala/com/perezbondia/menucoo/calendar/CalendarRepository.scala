package com.perezbondia.menucoo.calendar

import java.sql.Date
import java.time.LocalDate
import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink}
import com.perezbondia.menucoo.dishes.{Dish, DishesRepository}
import com.perezbondia.menucoo.dishes.Dish.DishId
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.lifted.PrimaryKey

import scala.concurrent.{ExecutionContext, Future}


object MenuType extends Enumeration {
  val lunch = Value
  val dinner = Value
}

case class DayMenuRow(day: LocalDate, menuType: MenuType.Value, dishId: DishId)

object DayMenuRow {
  def fromRequest(day: LocalDate, r: DayMenuRequest): Seq[DayMenuRow] =
    r.lunch.map(d => DayMenuRow(day, MenuType.lunch, d)) ++
      r.dinner.map(d => DayMenuRow(day, MenuType.dinner, d))

  def toDayMenu(day: LocalDate, rows: Seq[DayMenuRow], dishes: Map[DishId, Dish]): DayMenu = {
    val (lunch, dinner) = rows.partition(_.menuType == MenuType.lunch)
    DayMenu(day, lunch.map(d => dishes(d.dishId)), dinner.map(d => dishes(d.dishId)))
  }

}


trait CalendarSchema {
  val jdbcProfile: JdbcProfile

  import jdbcProfile.api._

  implicit def localDateToDate = MappedColumnType.base[LocalDate, Date](l => Date.valueOf(l), d => d.toLocalDate)
  implicit def menuTypeToString = MappedColumnType.base[MenuType.Value, String](mt => mt.toString, s => MenuType.withName(s))

  class DayMenuTable(tag: Tag) extends Table[DayMenuRow](tag, "day_menus") {

    def day = column[LocalDate]("day")

    def menuType = column[MenuType.Value]("menu_type")

    def dishId = column[UUID]("dish")

    def pk = primaryKey("pk_day_menus", (day, menuType, dishId))

    def * = (day, menuType, dishId) <> ((DayMenuRow.apply _).tupled, DayMenuRow.unapply)
  }

  val dayMenus = TableQuery[DayMenuTable]
}


class CalendarRepository(dbConfig: DatabaseConfig[JdbcProfile], dishesRepository: DishesRepository)(implicit ec: ExecutionContext) extends CalendarSchema {
  val jdbcProfile = dbConfig.profile

  import jdbcProfile.api._

  val db = dbConfig.db

  implicit val system: ActorSystem = ActorSystem("menucooAkka")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def getDayMenu(day: LocalDate): Future[DayMenu] = {
    val dishes = dishesRepository.getDishes.map(d=> (d.id, d)).toMat(Sink.seq)(Keep.right).run().map(_.toMap)
    dishes.flatMap(dss =>
      db.run(dayMenus.filter(_.day === day).result).map(d => d.groupBy(_.menuType))
        .map(dr => DayMenu(day,
          dr.getOrElse(MenuType.lunch, Seq.empty).map(d => dss(d.dishId)),
          dr.getOrElse(MenuType.dinner, Seq.empty).map(d => dss(d.dishId))))
    )
  }

  def setDayMenu(day: LocalDate, dm: DayMenuRequest): Unit = {
    val cleanDay = dayMenus.filter(_.day === day).delete
    val insert = DayMenuRow.fromRequest(day, dm).map(r =>
      (dayMenus returning dayMenus) += r
    )
    (cleanDay +: insert).foreach(i => db.run(i))
  }
}
