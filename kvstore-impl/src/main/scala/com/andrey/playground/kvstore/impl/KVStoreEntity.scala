package com.andrey.playground.kvstore.impl

import java.time.{Instant, LocalDateTime}

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{JsonMigration, JsonSerializer, JsonSerializerRegistry}
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json._

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
    KVStoreState("NA", Set.empty, Instant.now())
  }


  private def persistEvent(event: KVStoreEvent, ctx: CommandContext[Done]): Persist =
    ctx.thenPersist(event) { _ =>
      logger.debug(s"$entityId: Persisted event $event")
      ctx.reply(Done)
    }

  private def replyDone(ctx: CommandContext[Done]): Persist = {
    ctx.reply(Done)
    ctx.done
  }

  /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = { x =>
      Actions().onCommand[UpdateValueCommand, Done] {

        case (UpdateValueCommand(value), ctx, state) =>

          val event = ValueChangedEvent(value, Instant.now())

          logger.trace(s"$entityId: Changing value $event")
          if (state.value != value) persistEvent(event, ctx)
          else replyDone(ctx)

      }.onCommand[AddTagCommand, Done] {
        case (AddTagCommand(tag), ctx, state) =>

          val normalizedTag = tag.trim.toLowerCase

          val event = TagAddedEvent(normalizedTag, Instant.now())

          logger.trace(s"$entityId: Adding tag $normalizedTag")
          if (!state.tags.contains(normalizedTag)) persistEvent(event, ctx)
          else replyDone(ctx)
      }.onCommand[RemoveTagCommand, Done] {
        case (RemoveTagCommand(tag), ctx, state) =>

          val normalizedTag = tag.trim.toLowerCase

          logger.trace(s"$entityId: Removing tag $normalizedTag")
          val event = TagRemovedEvent(normalizedTag, Instant.now())

          if (state.tags.contains(normalizedTag)) persistEvent(event, ctx)
          else replyDone(ctx)
      }.onReadOnlyCommand[GetValueCommand, KVStoreState] {

        case (GetValueCommand(key), ctx, state) =>
          ctx.reply(state)

      }.onEvent {
        case (event@ValueChangedEvent(value, timestamp), state) =>

          logger.debug(s"$entityId: Replaying event $event")
          state.updateValue(value, timestamp)
        case (event@TagAddedEvent(tag, timestamp), state) =>

          logger.debug(s"$entityId: Replaying event $event")
          state.addTag(tag, timestamp)
        case (event@TagRemovedEvent(tag, timestamp), state) =>

          logger.debug(s"$entityId: Replaying event $event")
          state.removeTag(tag, timestamp)
      }
  }
}

/**
  * The current state held by the persistent entity.
  */
case class KVStoreState(value: String, tags: Set[String], timestamp: Instant) {
  def updateValue(newValue: String, timestamp: Instant) = copy(value = newValue, timestamp = timestamp)

  def addTag(tag: String, timestamp: Instant) = copy(tags = tags + tag, timestamp = timestamp)
  def removeTag(tag: String, timestamp: Instant) = copy(tags = tags - tag, timestamp = timestamp)
}

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
case class UpdateValueCommand(value: String) extends KVStoreCommand[Done]

object UpdateValueCommand {
  implicit val format: Format[UpdateValueCommand] = Json.format
}

case class AddTagCommand(tag: String) extends KVStoreCommand[Done]

object AddTagCommand {
  implicit val format: Format[AddTagCommand] = Json.format
}

case class RemoveTagCommand(tag: String) extends KVStoreCommand[Done]

object RemoveTagCommand {
  implicit val format: Format[RemoveTagCommand] = Json.format
}

case class GetValueCommand(key: String) extends KVStoreCommand[KVStoreState]

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


