package com.andrey.playground.kvstore.api

import play.api.libs.json.{Format, Json}

case class UpdateValueRequest(value: String)

object UpdateValueRequest {
  implicit val updateValue: Format[UpdateValueRequest] = Json.format[UpdateValueRequest]
}