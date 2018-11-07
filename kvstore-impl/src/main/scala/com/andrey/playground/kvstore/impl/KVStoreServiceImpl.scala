package com.andrey.playground.kvstore.impl

import akka.stream.Materializer
import akka.{Done, NotUsed}
import com.andrey.playground.kvstore.api
import com.andrey.playground.kvstore.api._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

class KVStoreServiceImpl(persistentEntityRegistry: PersistentEntityRegistry, repo: KVRepository)(implicit ec: ExecutionContext, materializer: Materializer) extends KVStoreService with LazyLogging {

  override def get(key: String) = ServiceCall { _ =>

    logger.debug(s"/api/get/$key")
    submit(key, GetValueCommand(key)).map(x => KeyValue(key, x.value, x.tags, x.timestamp))
  }

  override def set(key: String) = ServiceCall { request =>
    logger.debug(s"/api/set/$key = ${request.value}")
    submit(key, UpdateValueCommand(request.value))
  }

  private def submit[T](key: String, command: KVStoreCommand[T]) = {
    val ref = persistentEntityRegistry.refFor[KVStoreEntity](key)

    ref.ask(command)
  }

  /**
    * Example: curl -H "Content-Type: application/json" -X POST -d '{"value": "Hi"}' http://localhost:9000/api/tag/pretty/alice
    */
  override def addTag(tag: String, key: String): ServiceCall[NotUsed, Done] = ServiceCall { request =>
    logger.debug(s"/api/tag/$tag/$key")
    submit(key, AddTagCommand(tag))
  }

  /**
    * Example: curl -H "Content-Type: application/json" -X DELETE -d '{"value": "Hi"}' http://localhost:9000/api/tag/pretty/alice
    */
  override def removeTag(tag: String, key: String): ServiceCall[NotUsed, Done] = ServiceCall { request =>
    logger.debug(s"/api/tag/$tag/$key (DELETE)")
    submit(key, RemoveTagCommand(tag))
  }

  override def updatesTopic(): Topic[api.KVMessage] =
    TopicProducer.taggedStreamWithOffset(KVStoreEvent.Tag) {
      (tag, fromOffset) =>
        logger.info(s"Initializing event stream for tag $tag from offset $fromOffset")
        persistentEntityRegistry.eventStream(tag, fromOffset)
          .map(ev => {

            (convertToPublicApi(ev), ev.offset)
          })
    }

  private def convertToPublicApi(eventElement: EventStreamElement[KVStoreEvent]): api.KVMessage = {
    eventElement.event match {
      case ValueChangedEvent(value, timestamp) => api.ValueUpdatedMessage(eventElement.entityId, value, timestamp)
      case TagAddedEvent(tag, timestamp) => api.TagAddedMessage(eventElement.entityId, tag, timestamp)
      case TagRemovedEvent(tag, timestamp) => api.TagRemovedMessage(eventElement.entityId, tag, timestamp)
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

