package com.andrey.playground.kvstore.impl

import com.lightbend.lagom.scaladsl.playjson.{JsonMigration, JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json.{JsObject, JsString}

import scala.collection.immutable.Seq

/**
  * Akka serialization, used by both persistence and remoting, needs to have
  * serializers registered for every type serialized or deserialized. While it's
  * possible to use any serializer you want for Akka messages, out of the box
  * Lagom provides support for JSON, via this registry abstraction.
  *
  * The serializers are registered here, and then provided to Lagom in the
  * application loader.
  */
object KVStoreSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[UpdateValueCommand],
    JsonSerializer[GetValueCommand],
    JsonSerializer[ValueChangedEvent],
    JsonSerializer[KVStoreState]
  )

  private val valueChangedEventMigration = new JsonMigration(2) {
    override def transform(fromVersion: Int, json: JsObject): JsObject = {
      if (fromVersion < 2) {
        json + ("timestamp" -> JsString("2018-11-01T00:00:00.000Z"))
      } else {
        json
      }
    }
  }

  // https://www.lagomframework.com/documentation/1.4.x/scala/Serialization.html#Schema-Evolution
  override def migrations = Map[String, JsonMigration](
    classOf[ValueChangedEvent].getName -> valueChangedEventMigration
  )
}
