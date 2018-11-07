package com.andrey.playground.kvstore.impl

import akka.Done
import akka.stream.scaladsl.Sink
import com.andrey.playground.kvstore.api._
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{AsyncFunSuite, FunSuite, Matchers}

class KVStoreServiceAcceptanceTest extends AsyncFunSuite with Matchers with LazyLogging with ScalaFutures {

  val app = new KVStoreServiceClientApp()

  test("can update key value") {

    val key = "alice"
    for {
      _ <- app.kvStoreService.addTag("tag1", key).invoke()
      _ <- app.kvStoreService.set(key).invoke(UpdateValueRequest("1"))
      _ <- app.kvStoreService.addTag("tag2", key).invoke()
      _ <- app.kvStoreService.removeTag("tag1", key).invoke()
      v <- app.kvStoreService.get(key).invoke()
    }
      yield {
        v.value shouldBe "1"
        v.tags.contains("tag1") shouldBe false
        v.tags.contains("tag2") shouldBe true
      }
  }
}
