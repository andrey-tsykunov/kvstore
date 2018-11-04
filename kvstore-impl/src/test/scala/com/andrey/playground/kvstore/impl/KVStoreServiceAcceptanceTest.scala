package com.andrey.playground.kvstore.impl

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.andrey.playground.kvstore.api.{AkkaComponents, KVStoreService}
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.client.{ConfigurationServiceLocatorComponents, LagomClientApplication, ServiceClient}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{AsyncFunSuite, Matchers}
import play.api.Configuration
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContext

class KVStoreServiceAcceptanceTest extends AsyncFunSuite with Matchers with LazyLogging {

  logger.info("Starting")

  val app = new KVStoreServiceClientApp()

  test("can get key value") {

    app.kvStoreService.get("alice").invoke().map { it =>
      it should not be empty
    }
  }

}
