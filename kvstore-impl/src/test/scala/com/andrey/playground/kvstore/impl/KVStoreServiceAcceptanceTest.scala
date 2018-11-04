package com.andrey.playground.kvstore.impl

import akka.Done
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.andrey.playground.kvstore.api.{AkkaComponents, KVStoreService, ValueUpdatedMessage}
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.client.{ConfigurationServiceLocatorComponents, LagomClientApplication, ServiceClient}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{AsyncFunSuite, FunSuite, Matchers}
import play.api.Configuration
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContext

class KVStoreServiceAcceptanceTest extends AsyncFunSuite with Matchers with LazyLogging with ScalaFutures {

  //implicit val pc = PatienceConfig(Span(2, Seconds))

  logger.info("Starting")

  val app = new KVStoreServiceClientApp()

  test("can get key value") {

    val f = app.kvStoreService.get("alice").invoke()

    //whenReady(f) { it =>
    f.map { it =>
      it should not be empty
    }
  }

  ignore("subscribe to key value updates") {
    val source = app.kvStoreService.updatesTopic().subscribe
      .withGroupId("KVStoreServiceAcceptanceTest")
      .atMostOnceSource

    implicit val m = app.materializer

    val log = Sink.foreach[ValueUpdatedMessage] { v =>
      logger.debug(s"Received $v")
    }

    source.runWith(log).map { it =>

      // never completes
      it shouldBe Done
    }
  }

}
