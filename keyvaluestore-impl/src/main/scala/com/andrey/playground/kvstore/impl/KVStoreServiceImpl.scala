package com.andrey.playground.kvstore.impl

import com.andrey.playground.kvstore.api
import com.andrey.playground.kvstore.api.{KVStoreService}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}

/**
  * Implementation of the KeyvaluestoreService.
  */
class KVStoreServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends KVStoreService {

  override def get(id: String) = ServiceCall { _ =>
    val ref = persistentEntityRegistry.refFor[KVStoreEntity](id)

    // Ask the entity the Hello command.
    ref.ask(GetValueCommand(id))
  }

  override def set(id: String) = ServiceCall { request =>
    // Look up the KeyValueStore entity for the given ID.
    val ref = persistentEntityRegistry.refFor[KVStoreEntity](id)

    // Tell the entity to use the greeting message specified.
    ref.ask(UpdateValueCommand(request.value))
  }


  override def updatesTopic(): Topic[api.ValueUpdatedEvent] =
    TopicProducer.singleStreamWithOffset {
      fromOffset =>
        persistentEntityRegistry.eventStream(KVStoreEvent.Tag, fromOffset)
          .map(ev => (convertEvent(ev), ev.offset))
    }

  private def convertEvent(helloEvent: EventStreamElement[KVStoreEvent]): api.ValueUpdatedEvent = {
    helloEvent.event match {
      case ValueChangedEvent(msg) => api.ValueUpdatedEvent(helloEvent.entityId, msg)
    }
  }
}
