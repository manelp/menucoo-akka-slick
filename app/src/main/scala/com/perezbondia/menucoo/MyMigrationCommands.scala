package com.perezbondia.menucoo

import com.liyaos.forklift.slick.{ SlickCodegen, SlickMigrationCommands, SlickMigrationManager }

trait MyMigrationCommands extends SlickMigrationCommands {
  this: SlickMigrationManager with SlickCodegen =>

  override def migrateOp(options: Seq[String]): Unit = {
    val applyOpsOp = List(() => applyOp)

    if (notYetAppliedMigrations.nonEmpty) {
      for (op <- previewOps) op()
      for (op <- applyOpsOp) op()
    }
  }

  def productionStatusOp(): Unit = {
    val ny = notYetAppliedMigrations
    if (ny.isEmpty) {
      println("Your database is up to date")
    } else {
      println(s"Your database is outdated, not yet applied migrations: ${notYetAppliedMigrations.map(_.id).mkString(", ")}")
    }
  }
  def initDatabaseCommand(): Unit = {
    init
  }
}

object MyMigrations extends MyMigrationCommands
    with SlickMigrationCommands
    with SlickMigrationManager
    with SlickCodegen {

  def migrate(): Unit = {
    productionStatusOp()
    migrateOp(Seq())
  }
}