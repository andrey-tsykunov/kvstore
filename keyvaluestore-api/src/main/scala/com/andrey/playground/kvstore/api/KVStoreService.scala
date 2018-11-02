package com.andrey.playground.kvstore.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}

object KVStoreService  {
  val TOPIC_NAME = "kvUpdates"
}

/**
  * The KeyValueStore service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the KeyvaluestoreService.
  */
trait KVStoreService extends Service {

  /**
    * Example: curl http://localhost:9000/api/get/alice
    */
  def get(id: String): ServiceCall[NotUsed, String]

  /**
    * Example: curl -H "Content-Type: application/json" -X POST -d '{"value": "Hi"}' http://localhost:9000/api/get/alice
    */
  def set(id: String): ServiceCall[UpdateValueRequest, Done]


  /**
    * This gets published to Kafka.
    */
  def updatesTopic(): Topic[ValueUpdatedEvent]

  override final def descriptor = {
    import Service._
    // @formatter:off
    named("kvstore")
      .withCalls(
        pathCall("/api/get/:id", get _),
        pathCall("/api/set/:id", set _)
      )
      .withTopics(
        topic(KVStoreService.TOPIC_NAME, updatesTopic)
          // Kafka partitions messages, messages within the same partition will
          // be delivered in order, to ensure that all messages for the same user
          // go to the same partition (and hence are delivered in order with respect
          // to that user), we configure a partition key strategy that extracts the
          // name as the partition key.
          .addProperty(
            KafkaProperties.partitionKeyStrategy,
            PartitionKeyStrategy[ValueUpdatedEvent](_.key)
          )
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}

/**
  * The greeting message class.
  */
case class UpdateValueRequest(value: String)

object UpdateValueRequest {
  /**
    * Format for converting greeting messages to and from JSON.
    *
    * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
    */
  implicit val format: Format[UpdateValueRequest] = Json.format[UpdateValueRequest]
}



/**
  * The greeting message class used by the topic stream.
  * Different than [[UpdateValueRequest]], this message includes the name (id).
  */
case class ValueUpdatedEvent(key: String, value: String)

object ValueUpdatedEvent {
  /**
    * Format for converting greeting messages to and from JSON.
    *
    * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
    */
  implicit val format: Format[ValueUpdatedEvent] = Json.format[ValueUpdatedEvent]
}
