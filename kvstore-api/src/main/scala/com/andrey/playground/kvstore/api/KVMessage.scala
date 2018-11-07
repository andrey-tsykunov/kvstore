package com.andrey.playground.kvstore.api

import java.time.Instant

import play.api.libs.json._

sealed trait KVMessage {
  def key: String
}

case class ValueUpdatedMessage(key: String, value: String, timestamp: Instant) extends KVMessage

object ValueUpdatedMessage {
  implicit val format: Format[ValueUpdatedMessage] = Json.format[ValueUpdatedMessage]
}

case class TagAddedMessage(key: String, tag: String, timestamp: Instant) extends KVMessage
object TagAddedMessage {
  implicit val format = Json.format[TagAddedMessage]
}

case class TagRemovedMessage(key: String, tag: String, timestamp: Instant) extends KVMessage
object TagRemovedMessage {
  implicit val format = Json.format[TagRemovedMessage]
}

object KVMessage {

  import play.api.libs.json._
  import play.api.libs.functional.syntax._

  // https://stackoverflow.com/questions/17021847/noise-free-json-format-for-sealed-traits-with-play-2-2-library
//  def unapply(message: KVMessage): Option[(String, JsValue)] = {
//    val (prod: Product, sub) = message match {
//      case x: ValueUpdatedMessage => (x, Json.toJson(x)(ValueUpdatedMessage.format))
//      case x: TagAddedMessage => (x, Json.toJson(x)(TagAddedMessage.format))
//      case x: TagRemovedMessage => (x, Json.toJson(x)(TagRemovedMessage.format))
//    }
//    Some(prod.productPrefix -> sub)
//  }
//
//  def apply(`class`: String, data: JsValue): KVMessage = {
//    (`class` match {
//      case "ValueUpdatedMessage" => Json.fromJson[ValueUpdatedMessage](data)(ValueUpdatedMessage.format)
//      case "TagAddedMessage" => Json.fromJson[TagAddedMessage](data)(TagAddedMessage.format)
//      case "TagRemovedMessage" => Json.fromJson[TagRemovedMessage](data)(TagRemovedMessage.format)
//    }).get
//  }

  // https://www.lagomframework.com/documentation/1.4.x/scala/MessageBrokerApi.html
  // https://medium.com/@gauravsingharoy/how-to-add-defaults-for-missing-properties-in-scala-play-json-unmarshalling-70ff10b775e3
  // https://www.playframework.com/documentation/2.6.x/ScalaJsonCombinators
  implicit val reads: Reads[KVMessage] = {
    ((__ \ "_type").read[String] or Reads.pure("com.andrey.playground.kvstore.api.ValueUpdatedMessage")).flatMap {
      case "com.andrey.playground.kvstore.api.ValueUpdatedMessage" => implicitly[Reads[ValueUpdatedMessage]].map(identity)
      case "com.andrey.playground.kvstore.api.TagAddedMessage" => implicitly[Reads[TagAddedMessage]].map(identity)
      case "com.andrey.playground.kvstore.api.TagRemovedMessage" => implicitly[Reads[TagRemovedMessage]].map(identity)
      case other => Reads(_ => JsError(s"Unknown event type $other"))
    }
  }

  implicit val writes: Writes[KVMessage] = Writes { event =>
    val (jsValue, eventType) = event match {
      case m: ValueUpdatedMessage => (Json.toJson(m)(ValueUpdatedMessage.format), m.getClass.getName)
      case m: TagAddedMessage => (Json.toJson(m)(TagAddedMessage.format), m.getClass.getName)
      case m: TagRemovedMessage => (Json.toJson(m)(TagRemovedMessage.format), m.getClass.getName)
    }
    jsValue.transform(__.json.update((__ \ '_type).json.put(JsString(eventType)))).get
  }

  implicit val format = Format(reads, writes)
}