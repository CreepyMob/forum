import sbt.Keys.libraryDependencies

name := "forum"

version := "0.1"

scalaVersion := "2.12.8"


val catsVersion = "1.6.0"
val catsEffectVersion = "1.1.0"
val doobie = "0.6.0"
val circeVersion = "0.10.0"
val fs2Version = "1.0.4"
val akkaVersion = "2.5.22"
val akkaHttpVersion = "10.1.7"
val FlywayVersion = "5.2.4"

libraryDependencies += "org.typelevel" %% "cats-core" % catsVersion
libraryDependencies += "org.typelevel" %% "cats-effect" % catsEffectVersion

libraryDependencies += "org.tpolecat" %% "doobie-core" % doobie
libraryDependencies += "org.tpolecat" %% "doobie-postgres" % doobie
libraryDependencies += "org.tpolecat" %% "doobie-specs2" % doobie
libraryDependencies += "org.tpolecat" %% "doobie-hikari" % doobie

libraryDependencies += "org.flywaydb" % "flyway-core" % FlywayVersion

libraryDependencies += "io.circe" %% "circe-core" % circeVersion
libraryDependencies += "io.circe" %% "circe-parser" % circeVersion
libraryDependencies += "io.circe" %% "circe-generic" % circeVersion

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-remote" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http" % akkaHttpVersion

scalacOptions := Seq(
  "-encoding",
  "utf8",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-target:jvm-1.8",
  "-Ypartial-unification",
  "-language:_",
  "-Xexperimental"
)

addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M11" cross CrossVersion.patch)
