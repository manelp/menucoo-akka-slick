package com.perezbondia.menucoo

import java.net.URI

import scala.util.Properties

trait DatabaseUriConfig {

  lazy val dbUriPropertyName: String = "DATABASE_URL"

  val defaultDbUri: String

  val dbConnectionParamsPropertyName = "DATABASE_CONNECTION_PARAMS"

  val defaultDbConnectionParams = ""

  lazy val databaseUri = new URI(Properties.envOrElse(dbUriPropertyName, defaultDbUri))

  lazy val jdbcProtocol = databaseUri.getScheme.replaceAll("^postgres$", "postgresql")

  lazy val databaseHost = databaseUri.getHost

  lazy val databasePort = databaseUri.getPort

  lazy val databaseName = {
    val path = databaseUri.getPath
    if (path == null || path.length == 0)
      ""
    else if (!path.startsWith("/")) throw new IllegalArgumentException("Path of URL in " + dbUriPropertyName + " (" + path + ")must start with /")
    else
      path.substring(1)
  }

  lazy val databaseConnectionParams = Properties.envOrElse(dbConnectionParamsPropertyName, defaultDbConnectionParams)

  lazy val databaseConnectionUrl =
    "jdbc:" + jdbcProtocol + "://" + databaseHost + ":" + databasePort + "/" + databaseName + "?" + databaseConnectionParams

  lazy val databaseUsername = databaseUri.getUserInfo.split(":")(0)

  lazy val databasePassword = databaseUri.getUserInfo.split(":")(1)

}
