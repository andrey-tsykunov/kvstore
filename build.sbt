organization in ThisBuild := "com.andrey.playground"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.7"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
val h2 = "com.h2database" % "h2" % "1.4.197"
val alpakkaCassandra = "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % "1.0-M1"
val cassandraExtras = "com.datastax.cassandra" % "cassandra-driver-extras" % "3.2.0"
val cats = "org.typelevel" %% "cats-core" % "1.4.0"

//lagomKafkaPort in ThisBuild := 9092
//lagomKafkaZookeeperPort in ThisBuild := 2181
lagomKafkaEnabled in ThisBuild := false

//lagomCassandraPort in ThisBuild := 9042
//lagomCassandraEnabled in ThisBuild := false

lazy val `keyvaluestore` = (project in file("."))
  .aggregate(`kvstore-api`, `kvstore-impl`, `kvstore-stream-api`, `kvstore-stream-impl`)

lazy val `kvstore-api` = (project in file("kvstore-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      lagomScaladslClient,
      scalaLogging
    )
  )

lazy val `kvstore-impl` = (project in file("kvstore-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslPersistenceJdbc,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      cassandraExtras,
      h2,
      cats,
      macwire,
      scalaTest,
      alpakkaCassandra % Test
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`kvstore-api`)

lazy val `kvstore-stream-api` = (project in file("kvstore-stream-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `kvstore-stream-impl` = (project in file("kvstore-stream-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .dependsOn(`kvstore-stream-api`, `kvstore-api`)
