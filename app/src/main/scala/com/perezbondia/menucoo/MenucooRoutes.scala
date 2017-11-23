package com.perezbondia.menucoo

import akka.actor.ActorSystem
import akka.event.Logging

trait MenucooRoutes {

  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[MenucooRoutes])

}
