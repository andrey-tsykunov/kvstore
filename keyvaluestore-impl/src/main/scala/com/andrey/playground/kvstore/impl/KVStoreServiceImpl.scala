package com.andrey.playground.kvstore.impl

import akka.persistence.query.NoOffset
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.{Done, NotUsed}
import com.andrey.playground.kvstore.api
import com.andrey.playground.kvstore.api.{History, HistoryEvent, KVStoreService, SearchResult}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}

import scala.concurrent.ExecutionContext

class KVStoreServiceImpl(persistentEntityRegistry: PersistentEntityRegistry, repo: KVRepository)(implicit ec: ExecutionContext, materializer: Materializer) extends KVStoreService {

  override def get(id: String) = ServiceCall { _ =>
    val ref = persistentEntityRegistry.refFor[KVStoreEntity](id)

    ref.ask(GetValueCommand(id))
  }

  override def set(id: String) = ServiceCall { request =>

    val ref = persistentEntityRegistry.refFor[KVStoreEntity](id)

    ref.ask(UpdateValueCommand(request.value))
  }

  override def updatesTopic(): Topic[api.ValueUpdatedMessage] =
    TopicProducer.taggedStreamWithOffset(KVStoreEvent.Tag) {
      (tag, fromOffset) =>
        persistentEntityRegistry.eventStream(tag, fromOffset)
          .map(ev => (convertEvent(ev), ev.offset))
    }

  private def convertEvent(eventElement: EventStreamElement[KVStoreEvent]): api.ValueUpdatedMessage = {
    eventElement.event match {
      case ValueChangedEvent(value, timestamp) => api.ValueUpdatedMessage(eventElement.entityId, value, timestamp)
    }
  }

  override def history(key: String): ServiceCall[NotUsed, History] = ServiceCall { request =>
    repo.getHistory(key)
  }

  /**
    * Example: curl -H "Content-Type: application/json" -X POST -d '{"value": "Hi"}' http://localhost:9000/api/search
    */
  override def search(): ServiceCall[NotUsed, SearchResult] = ServiceCall { request =>
    repo.search()
  }
}

