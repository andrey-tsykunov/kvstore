package com.andrey.playground.kvstore.impl

import java.sql.Connection

import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, ReadSideProcessor}
import com.lightbend.lagom.scaladsl.persistence.jdbc.{JdbcReadSide, JdbcSession}
import com.typesafe.scalalogging.LazyLogging

import JdbcSession._

import scala.concurrent.ExecutionContext

class KeyValuesEventProcessor(readSide: JdbcReadSide)(implicit ec: ExecutionContext)
  extends ReadSideProcessor[KVStoreEvent] with LazyLogging {

  val createTableSql =
    "CREATE TABLE IF NOT EXISTS PUBLIC.keyvalues (key NVARCHAR2(64), value NVARCHAR2(512), timestamp TIMESTAMP WITH TIME ZONE, PRIMARY KEY (key))"

  val buildTables: Connection => Unit = { connection =>
    tryWith(connection.createStatement()) {
      _.executeUpdate(createTableSql)
    }
  }

  val processKeyValueChanged: (Connection, EventStreamElement[ValueChangedEvent]) => Unit = {
    (connection, eventElement) =>

      logger.debug(s"Saving event ${eventElement.entityId} @ offset ${eventElement.offset}: ${eventElement.event}")

      tryWith(
        // "MERGE" is H2's equivalent to 'INSERT OR UPDATE'.
        // See http://www.h2database.com/html/grammar.html#merge
        // We use "MERGE" here because we want this read-side to keep only the lastest message per each name
        // Since 'name' is the table Primary Key then merging is trivial.
        connection.prepareStatement("MERGE INTO keyvalues (key, value, timestamp) VALUES (?, ?, ?)")
      ) { statement =>
        statement.setString(1, eventElement.entityId)
        statement.setString(2, eventElement.event.value)
        statement.setTimestamp(3, java.sql.Timestamp.from( eventElement.event.timestamp ))
        statement.executeUpdate()
        logger.debug(s"Saved event ${eventElement.entityId} @ offset ${eventElement.offset}")
      }
  }

  override def buildHandler() =
    readSide
      .builder[KVStoreEvent]("KeyValueReadSide")
      .setGlobalPrepare(buildTables)
      .setEventHandler(processKeyValueChanged)
      .build()

  override def aggregateTags = KVStoreEvent.Tag.allTags
}
