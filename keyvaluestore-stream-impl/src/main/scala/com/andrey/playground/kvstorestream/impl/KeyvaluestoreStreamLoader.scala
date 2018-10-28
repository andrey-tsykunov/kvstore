package com.andrey.playground.kvstorestream.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.andrey.playground.kvstorestream.api.KeyvaluestoreStreamService
import com.andrey.playground.kvstore.api.KeyvaluestoreService
import com.softwaremill.macwire._

class KeyvaluestoreStreamLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new KeyvaluestoreStreamApplication(context) {
      override def serviceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new KeyvaluestoreStreamApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[KeyvaluestoreStreamService])
}

abstract class KeyvaluestoreStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[KeyvaluestoreStreamService](wire[KeyvaluestoreStreamServiceImpl])

  // Bind the KeyvaluestoreService client
  lazy val keyvaluestoreService = serviceClient.implement[KeyvaluestoreService]
}
