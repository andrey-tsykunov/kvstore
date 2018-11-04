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

/**
  * An event that represents a change in greeting message.
  */
case class ValueChangedEvent(value: String, timestamp: Instant) extends KVStoreEvent

object ValueChangedEvent {

  /**
    * Format for the greeting message changed event.
    *
    * Events get stored and loaded from the database, hence a JSON format
    * needs to be declared so that they can be serialized and deserialized.
    */
  implicit val format: Format[ValueChangedEvent] = Json.format
}