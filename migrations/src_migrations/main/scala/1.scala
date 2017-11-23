import slick.jdbc.H2Profile.api._
import com.liyaos.forklift.slick.SqlMigration

object M1 {
  MyMigrations.migrations = MyMigrations.migrations :+ SqlMigration(1)(List(
    sqlu"""create table "dishes" ("id" integer not null primary key, "name" varchar not null)""" // your sql code goes here
  ))
}
