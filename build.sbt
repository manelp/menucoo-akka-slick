import com.typesafe.sbt.packager.archetypes.JavaAppPackaging

name := "menucoo-akka"

addCommandAlias("mgm", "migrations_manager/run")

addCommandAlias("mg", "db/migration/run")

lazy val akkaHttpVersion = "10.0.10"
lazy val akkaVersion = "2.5.6"
lazy val akkaCirceV = "1.18.0"
lazy val circeV = "0.8.0"
lazy val slickV = "3.2.1"
lazy val forkliftVersion = "0.3.1"

enablePlugins(JavaAppPackaging)

enablePlugins(DockerPlugin)

lazy val commonSettings = Seq(
  version := "1.0",
  organization := "com.perezbondia",
  scalaVersion := "2.12.3",
  scalacOptions += "-deprecation",
  scalacOptions += "-feature",
  resolvers ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.url("https://flywaydb.org/repo")
  )
)



lazy val loggingDependencies = List(
  "ch.qos.logback" % "logback-classic" % "1.1.3"
)

lazy val akkaDependencies = List(
  "de.heikoseeberger" %% "akka-http-circe" % akkaCirceV,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "io.circe" %% "circe-core" % circeV,
  "io.circe" %% "circe-generic" % circeV,
  "io.circe" %% "circe-parser" % circeV
)

lazy val slickDependencies = List(
  "com.typesafe.slick" %% "slick" % slickV
)

lazy val dbDependencies = List(
  "org.flywaydb" % "flyway-core" % "4.2.0",
  "com.typesafe.slick" %% "slick-hikaricp" % slickV,
  "org.postgresql" % "postgresql" % "42.1.4"
)

lazy val testDependencies = List(
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.0.1" % Test
)

lazy val appDependencies = akkaDependencies ++ dbDependencies ++ loggingDependencies ++ testDependencies

lazy val app = (project in file("app"))
  .settings(commonSettings: _*)
  .settings {
    name := "menucoo-akka"
    libraryDependencies ++= appDependencies
  }

lazy val menucooAkka = Project("menucoo-akka", file("."))
  .dependsOn(app)
  .settings(commonSettings: _*)
  .settings(mainClass in Compile := Some("com.perezbondia.menucoo.Runner"))