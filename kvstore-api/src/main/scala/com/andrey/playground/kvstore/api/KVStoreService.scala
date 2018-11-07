package com.andrey.playground.kvstore.api

import java.time.Instant

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}

object KVStoreService  {
  val updatesTopicName = "kvstore"
  val name = "kvstore-svc"
}

case class KeyValue(key: String, value: String, tags: Set[String], timestamp: Instant)

object KeyValue {
  implicit val keyValue = Json.format[KeyValue]
}

case class SearchResult(keyValues: Seq[KeyValue])
object SearchResult {
  implicit val searchResult = Json.format[SearchResult]
}

case class HistoryEvent(key: String, value: String, timestamp: Instant)

case class History(events: Seq[HistoryEvent])

object History {
  implicit val historyEvent: Format[HistoryEvent] = Json.format[HistoryEvent]
  implicit val history: Format[History] = Json.format[History]
}

/**
  * The KeyValueStore service interface.
  * <p>
  */
trait KVStoreService extends Service {

  import KVStoreService._

  /**
    * Example: curl http://localhost:9000/api/get/alice
    */
  def get(key: String): ServiceCall[NotUsed, KeyValue]

  /**
    * Example: curl -H "Content-Type: application/json" -X POST -d '{"value": "Hi"}' http://localhost:9000/api/set/alice
    */
  def set(key: String): ServiceCall[UpdateValueRequest, Done]

  /**
    * Example: curl -H "Content-Type: application/json" -X POST -d '{"value": "Hi"}' http://localhost:9000/api/tag/pretty/alice
    */
  def addTag(tag: String, key: String): ServiceCall[NotUsed, Done]

  /**
    * Example: curl -H "Content-Type: application/json" -X DELETE -d '{"value": "Hi"}' http://localhost:9000/api/tag/pretty/alice
    */
  def removeTag(tag: String, key: String): ServiceCall[NotUsed, Done]

  /**
    * Example: curl -H "Content-Type: application/json" -X POST -d '{"value": "Hi"}' http://localhost:9000/api/history/alice
    */
  def history(key: String): ServiceCall[NotUsed, History]

  /**
    * Example: curl -H "Content-Type: application/json" -X POST -d '{"value": "Hi"}' http://localhost:9000/api/search
    */
  def search(): ServiceCall[NotUsed, SearchResult]

  /**
    * This gets published to Kafka.
    */
  def updatesTopic(): Topic[KVMessage]

  override final def descriptor = {
    import Service._
    // @formatter:off
    named(name)
      .withCalls(
        pathCall("/api/get/:key", get _),
        pathCall("/api/history/:key", history _),
        pathCall("/api/search", search _),
        pathCall("/api/set/:key", set _),
        restCall(Method.POST, "/api/tag/:tag/:key", addTag _),
        restCall(Method.DELETE, "/api/tag/:tag/:key", removeTag _)
      )
      .withTopics(
        topic(updatesTopicName, updatesTopic)
          // Kafka partitions messages, messages within the same partition will
          // be delivered in order, to ensure that all messages for the same user
          // go to the same partition (and hence are delivered in order with respect
          // to that user), we configure a partition key strategy that extracts the
          // name as the partition key.
          .addProperty(
            KafkaProperties.partitionKeyStrategy,
            PartitionKeyStrategy[KVMessage](_.key)
          )
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}
