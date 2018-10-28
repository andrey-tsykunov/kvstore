package com.andrey.playground.kvstorestream.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.andrey.playground.kvstorestream.api.KeyvaluestoreStreamService
import com.andrey.playground.kvstore.api.KeyvaluestoreService

import scala.concurrent.Future

/**
  * Implementation of the KeyvaluestoreStreamService.
  */
class KeyvaluestoreStreamServiceImpl(keyvaluestoreService: KeyvaluestoreService) extends KeyvaluestoreStreamService {
  def stream = ServiceCall { hellos =>
    Future.successful(hellos.mapAsync(8)(keyvaluestoreService.hello(_).invoke()))
  }
}
