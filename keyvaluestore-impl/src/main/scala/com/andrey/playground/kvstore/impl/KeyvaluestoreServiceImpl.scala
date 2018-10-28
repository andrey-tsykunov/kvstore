package com.andrey.playground.kvstore.impl

import com.andrey.playground.kvstore.api
import com.andrey.playground.kvstore.api.{KeyvaluestoreService}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}

/**
  * Implementation of the KeyvaluestoreService.
  */
class KeyvaluestoreServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends KeyvaluestoreService {

  override def hello(id: String) = ServiceCall { _ =>
    // Look up the KeyValueStore entity for the given ID.
    val ref = persistentEntityRegistry.refFor[KeyvaluestoreEntity](id)

    // Ask the entity the Hello command.
    ref.ask(Hello(id))
  }

  override def useGreeting(id: String) = ServiceCall { request =>
    // Look up the KeyValueStore entity for the given ID.
    val ref = persistentEntityRegistry.refFor[KeyvaluestoreEntity](id)

    // Tell the entity to use the greeting message specified.
    ref.ask(UseGreetingMessage(request.message))
  }


  override def greetingsTopic(): Topic[api.GreetingMessageChanged] =
    TopicProducer.singleStreamWithOffset {
      fromOffset =>
        persistentEntityRegistry.eventStream(KeyvaluestoreEvent.Tag, fromOffset)
          .map(ev => (convertEvent(ev), ev.offset))
    }

  private def convertEvent(helloEvent: EventStreamElement[KeyvaluestoreEvent]): api.GreetingMessageChanged = {
    helloEvent.event match {
      case GreetingMessageChanged(msg) => api.GreetingMessageChanged(helloEvent.entityId, msg)
    }
  }
}
