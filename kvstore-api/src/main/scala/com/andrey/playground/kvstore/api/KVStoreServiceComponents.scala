package com.andrey.playground.kvstore.api

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.client.ServiceClient
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

trait AkkaComponents {
  def config: Config
  implicit def actorSystem: ActorSystem
  implicit def materializer: Materializer
  implicit def executionContext: ExecutionContext
}

trait KVStoreServiceComponents extends LazyLogging { akka: AkkaComponents =>

  import KVStoreService._

  def serviceClient: ServiceClient
  def serviceLocator: ServiceLocator

  lazy val kvStoreService = serviceClient.implement[KVStoreService]

  serviceLocator.locate(name).foreach {
    case Some(uri) => logger.info(s"Initialized client for $name service using $uri")
    case _ =>
  }
}
