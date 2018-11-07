package com.lightbend.lagom.scaladsl.persistence.cassandra

/*
  https://discuss.lightbend.com/t/cassandra-read-side-and-a-jdbc-readside-event-processor-possible-in-lagom-1-4/881/4
  https://github.com/lagom/lagom/issues/1346
 */
trait CustomReadSideCassandraPersistenceComponents extends ReadSideCassandraPersistenceComponents {
  def getCassandraOffsetStore = cassandraOffsetStore
}
