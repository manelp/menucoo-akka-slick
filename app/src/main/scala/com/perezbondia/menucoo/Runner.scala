package com.perezbondia.menucoo

object Runner extends App {

  val ctx = new Context()
  import ctx._

  migrations.migrate()

  start()

}

object RunnerLocal extends App {

  val ctx = new Context()
  import ctx._

  migrations.migrate()

  start(dockerized = false)

}
