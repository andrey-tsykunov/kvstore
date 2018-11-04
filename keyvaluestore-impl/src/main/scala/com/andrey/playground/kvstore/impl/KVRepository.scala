package com.andrey.playground.kvstore.impl

import java.sql.{Connection, ResultSet}

import com.andrey.playground.kvstore.api.{History, HistoryEvent, KeyValue, SearchResult}
import com.lightbend.lagom.scaladsl.persistence.ReadSide
import com.lightbend.lagom.scaladsl.persistence.jdbc.JdbcSession
import com.lightbend.lagom.scaladsl.persistence.jdbc.JdbcSession.tryWith

import scala.concurrent.Future

class KVRepository(session: JdbcSession, readSide: ReadSide) {

  def getHistory(key: String): Future[History] = {
    session.withConnection { implicit conn: Connection =>
      tryWith(conn.prepareStatement("SELECT key, value, timestamp from history WHERE key = ?;")) { statement =>

        statement.setString(1, key)

        parseToHistoryEvent(statement.executeQuery())
      }
    }
  }

  def search(): Future[SearchResult] = {
    session.withConnection { conn: Connection =>
      tryWith(conn.prepareStatement("SELECT key, value, timestamp from keyvalues;").executeQuery) {

        parseToSearchResult
      }
    }
  }

  private def parseToHistoryEvent(rs: ResultSet): History = {
    val history = Vector.newBuilder[HistoryEvent]
    while (rs.next()) {
      history += HistoryEvent(
        rs.getString("key"),
        rs.getString("value"),
        rs.getTimestamp("timestamp").toInstant
      )
    }
    History(history.result())
  }

  private def parseToSearchResult(rs: ResultSet): SearchResult = {
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
