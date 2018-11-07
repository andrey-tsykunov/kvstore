package com.andrey.playground.kvstore.impl

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.cassandra.scaladsl.CassandraSource
import akka.stream.scaladsl.Sink
import com.andrey.playground.kvstore.api.KVMessage
import com.datastax.driver.core.{Cluster, Row, SimpleStatement}
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{AsyncFunSuite, FunSuite, Matchers}

class KvStoreExploratoryTest extends AsyncFunSuite with Matchers with LazyLogging {

  test("subscribe to kvstore messages") {

    val app = new KVStoreServiceClientApp()
    val source = app.kvStoreService.updatesTopic().subscribe
      .withGroupId("KVStoreServiceAcceptanceTest")
      .atMostOnceSource

    implicit val m = app.materializer

    source.runWith(logSink[KVMessage](_.toString)).map { it =>

      // never completes
      it shouldBe Done
    }
  }

  test("read kvstore messages from Cassandra") {
    implicit val session = Cluster.builder
      .addContactPoint("127.0.0.1")
      .withPort(4000)
      .build
      .connect()

    implicit val system = ActorSystem()
    implicit val mat = ActorMaterializer()

    val stmt = new SimpleStatement(s"SELECT * FROM kvstore.messages")

    val rows = CassandraSource(stmt)

    def print(row: Row): String = {
      s"${row.getString("persistence_id")}, #seq = ${row.getLong("sequence_nr")}, tag = ${row.getString("tag1")}"
    }

    rows.runWith(logSink(print)).map { it =>

      // never completes
      it shouldBe Done
    }
  }

  private def logSink[T](f: T => String) = Sink.foreach[T] { v =>
    logger.debug(s"Received message ${f(v)}")
  }
}
