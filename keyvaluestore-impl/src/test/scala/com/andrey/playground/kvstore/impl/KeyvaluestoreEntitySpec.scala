package com.andrey.playground.kvstore.impl

import java.time.Instant

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class KeyvaluestoreEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  private val system = ActorSystem("KeyvaluestoreEntitySpec",
    JsonSerializerRegistry.actorSystemSetupFor(KVStoreSerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def withTestDriver(block: PersistentEntityTestDriver[KVStoreCommand[_], KVStoreEvent, KVStoreState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new KVStoreEntity, "keyvaluestore-1")
    block(driver)
    driver.getAllIssues should have size 0
  }

  "KeyValueStore entity" should {

    "say hello by default" in withTestDriver { driver =>
      val outcome = driver.run(GetValueCommand("Alice"))
      outcome.replies should contain only "Hello, Alice!"
    }

    "allow updating the greeting message" in withTestDriver { driver =>
      val outcome1 = driver.run(UpdateValueCommand("Hi"))
      outcome1.events should contain only ValueChangedEvent("Hi", Instant.now())
      val outcome2 = driver.run(GetValueCommand("Alice"))
      outcome2.replies should contain only "Hi, Alice!"
    }

  }
}
