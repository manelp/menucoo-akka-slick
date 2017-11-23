import com.typesafe.sbt.packager.MappingsHelper._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging

name := "menucoo-akka"

addCommandAlias("mgm", "migrations_manager/run")

addCommandAlias("mg", "migrations/run")

lazy val akkaHttpVersion = "10.0.10"
lazy val akkaVersion = "2.5.6"
lazy val akkaCirceV = "1.18.0"
lazy val circeV = "0.8.0"
lazy val slickV = "3.2.1"
lazy val forkliftVersion = "0.3.1"

enablePlugins(JavaAppPackaging)

// -- mappings for the database migrations --
mappings in Universal ++= contentOf(baseDirectory.value / "migrations").map {
  case (file, dest) => file -> s"db/migrations/$dest"
}
mappings in Universal ++= contentOf(baseDirectory.value / "migration_manager").map {
  case (file, dest) => file -> s"db/migration_manager/$dest"
}
mappings in Universal ++= contentOf(baseDirectory.value / "generated_code").map {
  case (file, dest) => file -> s"db/generated_code/$dest"
}
mappings in Universal ++= contentOf(baseDirectory.value / "project").map {
  case (file, dest) => file -> s"db/project/$dest"
}
mappings in Universal ++= contentOf(baseDirectory.value / "app" / "src" / "main" / "resources").map {
  case (file, dest) => file -> s"db/app/src/main/resources/$dest"
}
mappings in Universal += {
  ((baseDirectory in Compile).value / "build.sbt") -> "db/build.sbt"
}

lazy val commonSettings = Seq(
  version := "1.0",
  organization := "com.perezbondia",
  scalaVersion := "2.12.3",
  scalacOptions += "-deprecation",
  scalacOptions += "-feature",
  resolvers ++= Seq(
    Resolver.bintrayRepo("naftoligug", "maven"),
    Resolver.sonatypeRepo("snapshots"))
)

lazy val loggingDependencies = List(
  "org.slf4j" % "slf4j-nop" % "1.6.4" // <- disables logging
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
  "com.typesafe.slick" %% "slick-hikaricp" % slickV,
  "com.h2database" % "h2" % "1.4.192"
)

lazy val forkliftDependencies = List(
  "com.liyaos" %% "scala-forklift-slick" % forkliftVersion,
  "io.github.nafg" %% "slick-migration-api" % "0.4.2"
)

lazy val testDependencies = List(
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.0.1" % Test
)

lazy val appDependencies = akkaDependencies ++ forkliftDependencies ++ dbDependencies ++ loggingDependencies ++ testDependencies

lazy val migrationsDependencies =
  dbDependencies ++ forkliftDependencies ++ loggingDependencies

lazy val migrationManagerDependencies = dbDependencies ++ forkliftDependencies

lazy val app = (project in file("app")).dependsOn(generatedCode)
  .settings(commonSettings: _*)
  .settings {
    name := "menucoo-akka"
    libraryDependencies ++= appDependencies
  }

lazy val migrationManager = Project("migration_manager",
  file("migration_manager")).settings(
  commonSettings: _*).settings {
  libraryDependencies ++= migrationManagerDependencies
}

lazy val migrations = Project("migrations",
  file("migrations")).dependsOn(
  generatedCode, migrationManager).settings(
  commonSettings: _*).settings {
  libraryDependencies ++= migrationsDependencies
}

lazy val tools = Project("git-tools",
  file("tools/git")).settings(commonSettings: _*).settings {
  libraryDependencies ++= forkliftDependencies ++ List(
    "com.liyaos" %% "scala-forklift-git-tools" % forkliftVersion,
    "com.typesafe" % "config" % "1.3.0",
    "org.eclipse.jgit" % "org.eclipse.jgit" % "4.0.1.201506240215-r"
  )
}

lazy val generatedCode = Project("generate_code",
  file("generated_code")).settings(commonSettings: _*).settings {
  libraryDependencies ++= slickDependencies
}

lazy val menucooAkka = Project("menucoo-akka", file("."))
  .aggregate(app, migrations, migrationManager, generatedCode, tools)
  .dependsOn(app)
  .settings(commonSettings: _*)
  .settings(mainClass in Compile := Some("com.perezbondia.menucoo.Runner"))