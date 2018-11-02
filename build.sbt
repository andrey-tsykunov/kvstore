organization in ThisBuild := "com.andrey.playground"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.7"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test

//lagomKafkaPort in ThisBuild := 9092
//lagomKafkaZookeeperPort in ThisBuild := 2181
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
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
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
