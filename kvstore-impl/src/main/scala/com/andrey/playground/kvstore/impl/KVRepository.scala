package com.andrey.playground.kvstore.impl

import java.sql.{Connection, ResultSet}

import cats.Applicative
import com.andrey.playground.kvstore.api.{History, HistoryEvent, KeyValue, SearchResult}
import com.lightbend.lagom.scaladsl.persistence.ReadSide
import com.lightbend.lagom.scaladsl.persistence.jdbc.JdbcSession
import com.lightbend.lagom.scaladsl.persistence.jdbc.JdbcSession.tryWith

import scala.concurrent.{ExecutionContext, Future}
import cats.implicits._

import scala.concurrent.ExecutionContext.Implicits.global

trait KVRepository {
  def getHistory(key: String): Future[History]
  def search(): Future[SearchResult]
}

object KVRepository {

  val empty = new KVRepository {
    override def getHistory(key: String): Future[History] = History(Nil).pure[Future]

    override def search(): Future[SearchResult] = SearchResult(Nil).pure[Future]
  }
}


