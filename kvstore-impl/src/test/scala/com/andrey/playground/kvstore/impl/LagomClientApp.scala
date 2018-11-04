package com.andrey.playground.kvstore.impl

import com.andrey.playground.kvstore.api.AkkaComponents
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaClientComponents
import com.lightbend.lagom.scaladsl.client.{ConfigurationServiceLocatorComponents, LagomClientApplication}
import play.api.Configuration
import play.api.libs.ws.ahc.AhcWSComponents

class LagomClientApp(name: String) extends LagomClientApplication(name)
  with ConfigurationServiceLocatorComponents
  with LagomKafkaClientComponents
  with AhcWSComponents
  with AkkaComponents {

  override lazy val configuration: Configuration = Configuration.load(environment, Map("akka.actor.provider" -> "local"))
}
