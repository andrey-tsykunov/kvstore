package com.andrey.playground.kvstore.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.andrey.playground.kvstore.api.KeyvaluestoreService
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.softwaremill.macwire._

class KeyvaluestoreLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new KeyvaluestoreApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new KeyvaluestoreApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[KeyvaluestoreService])
}

abstract class KeyvaluestoreApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[KeyvaluestoreService](wire[KeyvaluestoreServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = KeyvaluestoreSerializerRegistry

  // Register the KeyValueStore persistent entity
  persistentEntityRegistry.register(wire[KeyvaluestoreEntity])
}
