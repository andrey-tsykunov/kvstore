package com.andrey.playground.kvstore.impl

import java.sql.Connection

import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, ReadSideProcessor}
import com.lightbend.lagom.scaladsl.persistence.jdbc.{JdbcReadSide, JdbcSession}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

class HistoryEventProcessor(readSide: JdbcReadSide)(implicit ec: ExecutionContext)
  extends ReadSideProcessor[KVStoreEvent] with LazyLogging {

  val createTableSql =
    "CREATE TABLE IF NOT EXISTS PUBLIC.history (key NVARCHAR2(64), value NVARCHAR2(512), timestamp TIMESTAMP WITH TIME ZONE);CREATE INDEX IF NOT EXISTS PUBLIC.history_key_index ON PUBLIC.history (key);"

  val buildTables: Connection => Unit = { connection =>
    JdbcSession.tryWith(connection.createStatement()) {
      _.executeUpdate(createTableSql)
    }
  }

  val processKeyValueChanged: (Connection, EventStreamElement[ValueChangedEvent]) => Unit = {
    (connection, eventElement) =>

      logger.debug(s"Saving event ${eventElement.entityId} @ offset ${eventElement.offset}: ${eventElement.event}")

      JdbcSession.tryWith(
        // "MERGE" is H2's equivalent to 'INSERT OR UPDATE'.
        // See http://www.h2database.com/html/grammar.html#merge
        // We use "MERGE" here because we want this read-side to keep only the lastest message per each name
        // Since 'name' is the table Primary Key then merging is trivial.
        connection.prepareStatement("INSERT INTO history (key, value, timestamp) VALUES (?, ?, ?)")
      ) { statement =>
        statement.setString(1, eventElement.entityId)
        statement.setString(2, eventElement.event.value)
        statement.setTimestamp(3, java.sql.Timestamp.from( eventElement.event.timestamp ))
        statement.executeUpdate()
      }
  }

  override def buildHandler() =
    readSide
      .builder[KVStoreEvent]("HistoryReadSide")
      .setGlobalPrepare(buildTables)
      .setEventHandler(processKeyValueChanged)
      .build()

  override def aggregateTags = KVStoreEvent.Tag.allTags
}
