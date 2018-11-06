package com.andrey.playground.kvstore.impl

import java.time.Instant

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import play.api.libs.json.{Format, Json}

/**
  * This interface defines all the events that the KeyvaluestoreEntity supports.
  */
sealed trait KVStoreEvent extends AggregateEvent[KVStoreEvent] {
  def aggregateTag = KVStoreEvent.Tag
}

object KVStoreEvent {
  //val Tag = AggregateEventTag[KVStoreEvent]

  val NumShards = 4
  val Tag = AggregateEventTag.sharded[KVStoreEvent]("KVStoreEvent", NumShards)
}

case class ValueChangedEvent(value: String, timestamp: Instant) extends KVStoreEvent

object ValueChangedEvent {

  implicit val format: Format[ValueChangedEvent] = Json.format[ValueChangedEvent]
}

case class TagAddedEvent(tag: String, timestamp: Instant) extends KVStoreEvent

object TagAddedEvent {

  implicit val format: Format[TagAddedEvent] = Json.format[TagAddedEvent]
}

case class TagRemovedEvent(tag: String, timestamp: Instant) extends KVStoreEvent

object TagRemovedEvent {

  implicit val format: Format[TagRemovedEvent] = Json.format[TagRemovedEvent]
}