organization in ThisBuild := "com.andrey.playground"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.7"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
val h2 = "com.h2database" % "h2" % "1.4.197"

//lagomKafkaPort in ThisBuild := 9092
//lagomKafkaZookeeperPort in ThisBuild := 2181
lagomKafkaEnabled in ThisBuild := false

//lagomCassandraPort in ThisBuild := 4000

lazy val `keyvaluestore` = (project in file("."))
  .aggregate(`keyvaluestore-api`, `keyvaluestore-impl`, `keyvaluestore-stream-api`, `keyvaluestore-stream-impl`)

lazy val `keyvaluestore-api` = (project in file("keyvaluestore-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `keyvaluestore-impl` = (project in file("keyvaluestore-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslPersistenceJdbc,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      h2,
      macwire,
      scalaTest,
      scalaLogging,
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`keyvaluestore-api`)

lazy val `keyvaluestore-stream-api` = (project in file("keyvaluestore-stream-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `keyvaluestore-stream-impl` = (project in file("keyvaluestore-stream-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .dependsOn(`keyvaluestore-stream-api`, `keyvaluestore-api`)
