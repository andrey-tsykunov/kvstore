package com.andrey.playground.kvstore.impl

import java.sql.{Connection, ResultSet}

import com.andrey.playground.kvstore.api.{History, KeyValue, SearchResult}
import com.lightbend.lagom.scaladsl.persistence.ReadSide
import com.lightbend.lagom.scaladsl.persistence.jdbc.JdbcSession
import com.lightbend.lagom.scaladsl.persistence.jdbc.JdbcSession.tryWith

import scala.concurrent.Future

class KVRepository(session: JdbcSession, readSide: ReadSide) {

  def getHistory(): Future[History] = {
    Future.successful(History(Nil))
  }

  def search(): Future[SearchResult] = {
    session.withConnection { conn: Connection =>
      tryWith(conn.prepareStatement("SELECT key, value, timestamp from keyvalues;").executeQuery) {
        parse
      }
    }
  }

  private def parse(rs: ResultSet): SearchResult = {
    val keyValues = Vector.newBuilder[KeyValue]
    while (rs.next()) {
      keyValues += KeyValue(
        rs.getString("key"),
        rs.getString("value"),
        rs.getTimestamp("timestamp").toInstant
      )
    }
    SearchResult(keyValues.result())
  }
}
