package com.andrey.playground.kvstore.impl

import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import com.andrey.playground.kvstore.api._

class KeyvaluestoreServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new KVStoreApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[KVStoreService]

  override protected def afterAll() = server.stop()

  "KeyValueStore service" should {

    "say hello" in {
      client.get("Alice").invoke().map { answer =>
        answer should ===("Hello, Alice!")
      }
    }

    "allow responding with a custom message" in {
      for {
        _ <- client.set("Bob").invoke(UpdateValueRequest("Hi"))
        answer <- client.get("Bob").invoke()
      } yield {
        answer should ===("Hi, Bob!")
      }
    }
  }
}
