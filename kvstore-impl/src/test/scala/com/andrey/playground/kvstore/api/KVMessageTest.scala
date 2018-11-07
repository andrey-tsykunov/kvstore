package com.andrey.playground.kvstore.api

import java.time.Instant

import org.scalatest.{FunSuite, Matchers}
import play.api.libs.json.{JsSuccess, Json}

class KVMessageTest extends FunSuite with Matchers {

  test("can parse old format") {
    val json = Json.parse("""{"key":"aaa","value":"1","timestamp":"2018-11-07T02:44:16.632Z"}""")

    val expected = ValueUpdatedMessage("aaa", "1", Instant.ofEpochMilli(1541558656632L))

    ValueUpdatedMessage.format.reads(json) shouldBe JsSuccess(expected)
    KVMessage.format.reads(json) shouldBe JsSuccess(expected)
  }

}
