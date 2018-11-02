package com.andrey.playground.kvstorestream.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.andrey.playground.kvstorestream.api.KVStoreStreamService
import com.andrey.playground.kvstore.api.KVStoreService
import com.softwaremill.macwire._

class KVStoreStreamLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new KVStoreStreamApplication(context) {
      override def serviceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new KVStoreStreamApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[KVStoreStreamService])
}

abstract class KVStoreStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[KVStoreStreamService](wire[KVStoreStreamServiceImpl])

  // Bind the KeyvaluestoreService client
  lazy val keyvaluestoreService = serviceClient.implement[KVStoreService]
}
