package com.andrey.playground.kvstore.impl

import akka.{Done, NotUsed}
import akka.persistence.query.{NoOffset, Offset}
import akka.stream.scaladsl.Flow
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor.ReadSideHandler
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, EventStreamElement, ReadSideProcessor}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

class DebugEventProcessor(implicit ec: ExecutionContext) extends ReadSideProcessor[KVStoreEvent] with LazyLogging {

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[KVStoreEvent] = new ReadSideHandler[KVStoreEvent] {

    override def globalPrepare(): Future[Done] = {
      logger.info("Global prepare")
      Future.successful(Done)
    }

    override def prepare(tag: AggregateEventTag[KVStoreEvent]): Future[Offset] = {

      logger.info(s"Prepare for tag: $tag")
      Future.successful(NoOffset)
    }

    override def handle(): Flow[EventStreamElement[KVStoreEvent], Done, NotUsed] = {
      Flow[EventStreamElement[KVStoreEvent]]
        .mapAsync(1) { eventElement =>

          Future {
            logger.info(s"Observed event ${eventElement.entityId} @ offset ${eventElement.offset}: ${eventElement.event}")
            Done
          }
        }
    }
  }

  override def aggregateTags = KVStoreEvent.Tag.allTags
}
