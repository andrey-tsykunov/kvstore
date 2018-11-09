package com.andrey.playground.kvstore.impl

import com.datastax.driver.core.{BoundStatement, PreparedStatement}
import com.lightbend.lagom.scaladsl.persistence.EventStreamElement
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}

trait HandlersBuilder extends StrictLogging {
  type Handler[E] = EventStreamElement[E] => Future[scala.collection.immutable.Seq[BoundStatement]]
  type Setup[E] = (BoundStatement, EventStreamElement[E]) => Unit
  type StatementSetup[E] = (Future[PreparedStatement], Setup[E])

  def buildEventHandler[E](prepared: StatementSetup[E]*)(implicit ec: ExecutionContext): Handler[E] = { eventElement =>
    logger.debug(s"Saving event ${eventElement.entityId} @ offset ${eventElement.offset}: ${eventElement.event}")

    val statementsF = prepared.map { case (f: Future[PreparedStatement], setup) =>

      f.map(st => {
        val bound = st.bind()
        setup(bound, eventElement)
        bound
      })
    }.toList

    Future.sequence(statementsF)
  }
}
