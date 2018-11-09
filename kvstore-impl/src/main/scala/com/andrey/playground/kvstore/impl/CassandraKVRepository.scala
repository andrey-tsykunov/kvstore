package com.andrey.playground.kvstore.impl

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.andrey.playground.kvstore.api.{History, HistoryEvent, KeyValue, SearchResult}

import scala.concurrent.{ExecutionContext, Future}
import com.datastax.driver.core.{Row, TypeTokens}
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import com.datastax.driver.extras.codecs.jdk8.InstantCodec

import scala.collection.JavaConverters._

class CassandraKVRepository(session: CassandraSession)(implicit ec: ExecutionContext, materializer: Materializer) extends KVRepository {
  override def getHistory(key: String): Future[History] = {
    session.select(s"SELECT key, value, timestamp FROM history WHERE key = ?", key)
      .map(convertToHistoryEvent)
      .runWith(Sink.seq)
      .map(History(_))
  }

  override def search(): Future[SearchResult] = {
    session.select(s"SELECT key, value, tags, timestamp FROM kvstate")
      .map(convertToKeyValue)
      .runWith(Sink.seq)
      .map(SearchResult(_))
  }

  private def convertToKeyValue(row: Row): KeyValue = {
    KeyValue(
      row.getString("key"),
      row.getString("value"),
      row.getSet("tags", classOf[String]).asScala.toSet,
      row.get("timestamp", InstantCodec.instance)
    )
  }

  private def convertToHistoryEvent(row: Row): HistoryEvent = {
    HistoryEvent(
      row.getString("key"),
      row.getString("value"),
      row.get("timestamp", InstantCodec.instance)
    )
  }
}
