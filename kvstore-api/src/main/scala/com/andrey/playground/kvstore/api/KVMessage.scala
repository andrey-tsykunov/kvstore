package com.andrey.playground.kvstore.api

import java.time.Instant

import play.api.libs.json.{Format, JsValue, Json}

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

  def unapply(message: KVMessage): Option[(String, JsValue)] = {
    val (prod: Product, sub) = message match {
      case x: ValueUpdatedMessage => (x, Json.toJson(x)(ValueUpdatedMessage.format))
      case x: TagAddedMessage => (x, Json.toJson(x)(TagAddedMessage.format))
      case x: TagRemovedMessage => (x, Json.toJson(x)(TagRemovedMessage.format))
    }
    Some(prod.productPrefix -> sub)
  }

  def apply(`class`: String, data: JsValue): KVMessage = {
    (`class` match {
      case "ValueUpdatedMessage" => Json.fromJson[ValueUpdatedMessage](data)(ValueUpdatedMessage.format)
      case "TagAddedMessage" => Json.fromJson[TagAddedMessage](data)(TagAddedMessage.format)
      case "TagRemovedMessage" => Json.fromJson[TagRemovedMessage](data)(TagRemovedMessage.format)
    }).get
  }

  implicit val kvMessage: Format[KVMessage] = Json.format[KVMessage]
}