package com.andrey.playground.kvstore.impl

import java.time.{Instant, LocalDateTime}

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{JsonMigration, JsonSerializer, JsonSerializerRegistry}
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json._

import scala.collection.immutable.Seq

/**
  * This is an event sourced entity. It has a state, [[KVStoreState]], which
  * stores what the greeting should be (eg, "Hello").
  *
  * Event sourced entities are interacted with by sending them commands. This
  * entity supports two commands, a [[UpdateValueCommand]] command, which is
  * used to change the greeting, and a [[GetValueCommand]] command, which is a read
  * only command which returns a greeting to the name specified by the command.
  *
  * Commands get translated to events, and it's the events that get persisted by
  * the entity. Each event will have an event handler registered for it, and an
  * event handler simply applies an event to the current state. This will be done
  * when the event is first created, and it will also be done when the entity is
  * loaded from the database - each event will be replayed to recreate the state
  * of the entity.
  *
  * This entity defines one event, the [[ValueChangedEvent]] event,
  * which is emitted when a [[UpdateValueCommand]] command is received.
  */
class KVStoreEntity extends PersistentEntity with LazyLogging {

  override type Command = KVStoreCommand[_]
  override type Event = KVStoreEvent
  override type State = KVStoreState

  override def recoveryCompleted(state: KVStoreState): KVStoreState = {

    logger.debug(s"$entityId: Recovered")
    super.recoveryCompleted(state)
  }

  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState: KVStoreState = {
    logger.debug(s"$entityId: Created entity")
    KVStoreState("NA", Instant.now())
  }

  /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = {
    case KVStoreState(currentValue, _) => Actions().onCommand[UpdateValueCommand, Done] {

      case (UpdateValueCommand(value), ctx, state) =>

        val event = ValueChangedEvent(value, Instant.now())

        logger.debug(s"$entityId: Persisting event $event")

        ctx.thenPersist(event) { _ =>
          logger.debug(s"$entityId: Persisted event $event")
          ctx.reply(Done)
        }

    }.onReadOnlyCommand[GetValueCommand, String] {

      case (GetValueCommand(key), ctx, state) =>
        ctx.reply(s"$key = $currentValue")

    }.onEvent {
      case (event @ ValueChangedEvent(value, timestamp), state) =>

        logger.debug(s"$entityId: Replaying event $event")
        KVStoreState(value, timestamp)
    }
  }
}

/**
  * The current state held by the persistent entity.
  */
case class KVStoreState(value: String, timestamp: Instant)

object KVStoreState {
  /**
    * Format for the hello state.
    *
    * Persisted entities get snapshotted every configured number of events. This
    * means the state gets stored to the database, so that when the entity gets
    * loaded, you don't need to replay all the events, just the ones since the
    * snapshot. Hence, a JSON format needs to be declared so that it can be
    * serialized and deserialized when storing to and from the database.
    */
  implicit val format: Format[KVStoreState] = Json.format
}

/**
  * This interface defines all the commands that the HelloWorld entity supports.
  */
sealed trait KVStoreCommand[R] extends ReplyType[R]

/**
  * A command to switch the greeting message.
  *
  * It has a reply type of [[Done]], which is sent back to the caller
  * when all the events emitted by this command are successfully persisted.
  */
case class UpdateValueCommand(message: String) extends KVStoreCommand[Done]

object UpdateValueCommand {

  /**
    * Format for the use greeting message command.
    *
    * Persistent entities get sharded across the cluster. This means commands
    * may be sent over the network to the node where the entity lives if the
    * entity is not on the same node that the command was issued from. To do
    * that, a JSON format needs to be declared so the command can be serialized
    * and deserialized.
    */
  implicit val format: Format[UpdateValueCommand] = Json.format
}

/**
  * A command to say hello to someone using the current greeting message.
  *
  * The reply type is String, and will contain the message to say to that
  * person.
  */
case class GetValueCommand(name: String) extends KVStoreCommand[String]

object GetValueCommand {

  /**
    * Format for the hello command.
    *
    * Persistent entities get sharded across the cluster. This means commands
    * may be sent over the network to the node where the entity lives if the
    * entity is not on the same node that the command was issued from. To do
    * that, a JSON format needs to be declared so the command can be serialized
    * and deserialized.
    */
  implicit val format: Format[GetValueCommand] = Json.format
}


