package com.andrey.playground.kvstorestream.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.andrey.playground.kvstorestream.api.KVStoreStreamService
import com.andrey.playground.kvstore.api.KVStoreService

import scala.concurrent.{ExecutionContext, Future}

class KVStoreStreamServiceImpl(kvstoreService: KVStoreService)(implicit ec: ExecutionContext) extends KVStoreStreamService {
  def stream = ServiceCall { keys =>
    val source = keys.mapAsync(8) {
      kvstoreService.get(_).invoke().map { s =>
        s.value
      }
    }

    Future.successful(source)
  }
}
