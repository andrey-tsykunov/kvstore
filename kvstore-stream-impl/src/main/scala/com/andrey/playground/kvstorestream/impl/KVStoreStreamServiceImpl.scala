package com.andrey.playground.kvstorestream.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.andrey.playground.kvstorestream.api.KVStoreStreamService
import com.andrey.playground.kvstore.api.KVStoreService

import scala.concurrent.Future

/**
  * Implementation of the KeyvaluestoreStreamService.
  */
class KVStoreStreamServiceImpl(kvstoreService: KVStoreService) extends KVStoreStreamService {
  def stream = ServiceCall { keys =>
    Future.successful(keys.mapAsync(8)(kvstoreService.get(_).invoke()))
  }
}
