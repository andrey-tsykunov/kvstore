package com.andrey.playground.kvstore.impl

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class KeyvaluestoreEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  private val system = ActorSystem("KeyvaluestoreEntitySpec",
    JsonSerializerRegistry.actorSystemSetupFor(KeyvaluestoreSerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def withTestDriver(block: PersistentEntityTestDriver[KeyvaluestoreCommand[_], KeyvaluestoreEvent, KeyvaluestoreState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new KeyvaluestoreEntity, "keyvaluestore-1")
    block(driver)
    driver.getAllIssues should have size 0
  }

  "KeyValueStore entity" should {

    "say hello by default" in withTestDriver { driver =>
      val outcome = driver.run(Hello("Alice"))
      outcome.replies should contain only "Hello, Alice!"
    }

    "allow updating the greeting message" in withTestDriver { driver =>
      val outcome1 = driver.run(UseGreetingMessage("Hi"))
      outcome1.events should contain only GreetingMessageChanged("Hi")
      val outcome2 = driver.run(Hello("Alice"))
      outcome2.replies should contain only "Hi, Alice!"
    }

  }
}
