package com.andrey.playground.kvstorestream.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}

/**
  * The KeyValueStore stream interface.
  *
  * This describes everything that Lagom needs to know about how to serve and
  * consume the KeyvaluestoreStream service.
  */
trait KVStoreStreamService extends Service {

  def stream: ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  override final def descriptor = {
    import Service._

    named("keyvaluestore-stream")
      .withCalls(
        namedCall("stream", stream)
      ).withAutoAcl(true)
  }
}

