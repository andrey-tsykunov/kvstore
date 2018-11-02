package com.andrey.playground.kvstorestream.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.andrey.playground.kvstorestream.api.KVStoreStreamService
import com.andrey.playground.kvstore.api.KVStoreService

import scala.concurrent.Future

/**
  * Implementation of the KeyvaluestoreStreamService.
  */
class KVStoreStreamServiceImpl(keyvaluestoreService: KVStoreService) extends KVStoreStreamService {
  def stream = ServiceCall { hellos =>
    Future.successful(hellos.mapAsync(8)(keyvaluestoreService.get(_).invoke()))
  }
}
