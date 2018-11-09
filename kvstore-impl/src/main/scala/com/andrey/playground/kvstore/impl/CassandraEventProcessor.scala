package com.andrey.playground.kvstore.impl

import akka.Done
import com.datastax.driver.core.{BoundStatement, PreparedStatement}
import com.datastax.driver.extras.codecs.jdk8.InstantCodec
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, ReadSideProcessor}

import scala.collection.JavaConverters._

import scala.concurrent.{ExecutionContext, Future, Promise}

class CassandraEventProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit ec: ExecutionContext)
  extends ReadSideProcessor[KVStoreEvent] with HandlersBuilder {

  private def createTable(): Future[Done] =
    for {
    _ <- session.executeCreateTable("CREATE TABLE IF NOT EXISTS kvstate (key TEXT, value TEXT, tags set<TEXT>, timestamp TIMESTAMP, PRIMARY KEY (key))")
    _ <- session.executeCreateTable("CREATE TABLE IF NOT EXISTS history (key TEXT, value TEXT, timestamp TIMESTAMP, PRIMARY KEY (key, timestamp))")
    }
      yield Done

  private val valueChangedPromise = Promise[PreparedStatement]
  private val addTagPromise = Promise[PreparedStatement]
  private val removeTagPromise = Promise[PreparedStatement]
  private val addHistoryPromise = Promise[PreparedStatement]

  private val addHistory: StatementSetup[ValueChangedEvent] = (addHistoryPromise.future, { (s, elem) =>
    s.setString("key", elem.entityId)
    s.setString("value", elem.event.value)
    s.set("timestamp", elem.event.timestamp, InstantCodec.instance)
  })

  private val updateValue: StatementSetup[ValueChangedEvent] = (valueChangedPromise.future, { (s, elem) =>
      s.setString("key", elem.entityId)
      s.setString("value", elem.event.value)
      s.set("timestamp", elem.event.timestamp, InstantCodec.instance)
  })

  private val addTag: StatementSetup[TagAddedEvent] = (addTagPromise.future, { (s, elem) =>
    s.setSet("tags", Set(elem.event.tag).asJava)
    s.setString("key", elem.entityId)
  })

  private val removeTag: StatementSetup[TagRemovedEvent] = (removeTagPromise.future, { (s, elem) =>
    s.setSet("tags", Set(elem.event.tag).asJava)
    s.setString("key", elem.entityId)
  })

  private def prepareStatements(): Future[Done] = {

    logger.debug("Prepare statements")

    valueChangedPromise.completeWith(session.prepare("INSERT INTO kvstate (key, value, timestamp) VALUES (?, ?, ?)"))
    addTagPromise.completeWith(session.prepare("UPDATE kvstate SET tags = tags + ? WHERE key = ?"))
    removeTagPromise.completeWith(session.prepare("UPDATE kvstate SET tags = tags - ? WHERE key = ?"))
    addHistoryPromise.completeWith(session.prepare("INSERT INTO history (key, value, timestamp) VALUES (?, ?, ?)"))

    Future.sequence(
      List(valueChangedPromise, addTagPromise, removeTagPromise, addHistoryPromise).map(_.future)
    ).map(_ => Done)
  }

  override def buildHandler() =
    readSide
      .builder[KVStoreEvent]("CurrentStateReadSide")
      .setGlobalPrepare(createTable)
      .setPrepare(_ => prepareStatements())
      .setEventHandler(buildEventHandler(updateValue, addHistory))
      .setEventHandler(buildEventHandler(addTag))
      .setEventHandler(buildEventHandler(removeTag))
      .build()

  override def aggregateTags = KVStoreEvent.Tag.allTags
}
