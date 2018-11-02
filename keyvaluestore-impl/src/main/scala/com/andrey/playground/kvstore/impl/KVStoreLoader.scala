package com.andrey.playground.kvstore.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.andrey.playground.kvstore.api.KVStoreService
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.softwaremill.macwire._

class KVStoreLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new KVStoreApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new KVStoreApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[KVStoreService])
}

abstract class KVStoreApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[KVStoreService](wire[KVStoreServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = KVStoreSerializerRegistry

  // Register the KeyValueStore persistent entity
  persistentEntityRegistry.register(wire[KVStoreEntity])
}
