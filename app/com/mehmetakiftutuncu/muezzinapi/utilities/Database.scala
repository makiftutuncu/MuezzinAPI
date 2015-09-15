package com.mehmetakiftutuncu.muezzinapi.utilities

import java.sql.Connection

import anorm.{Row, SimpleSql}
import play.api.Play.current
import play.api.db.DB

/**
 * A utility object to allow database operations
 */
object Database {
  /**
   * Acquires a database connection and performs an apply
   *
   * @param sql Query to apply
   *
   * @return A list of [[anorm.Row]]s as the result of query
   */
  def applyWithConnection(sql: SimpleSql[Row]): List[Row] = {
    DB.withConnection {
      implicit connection =>
        apply(sql)
    }
  }

  /**
   * Performs an update on database
   *
   * @param sql Query to execute
   *
   * @return Number of affected rows by the update
   */
  def apply(sql: SimpleSql[Row])(implicit connection: Connection): List[Row] = {
    sql.withQueryTimeout(Option(Conf.dbTimeout)).fold(List.empty[Row]) {
      case (rows: List[Row], row: Row) =>
        rows :+ row
    }.right.get
  }

  /**
   * Acquires a database connection and performs an update
   *
   * @param sql Query to execute
   *
   * @return Number of affected rows by the update
   */
  def executeUpdate(sql: SimpleSql[Row])(implicit connection: Connection): Int = {
    sql.withQueryTimeout(Option(Conf.dbTimeout)).executeUpdate()
  }

  /**
   * Performs an update on database
   *
   * @param sql Query to execute
   *
   * @return Number of affected rows by the update
   */
  def executeUpdateWithConnection(sql: SimpleSql[Row]): Int = {
    DB.withConnection {
      implicit connection =>
        executeUpdate(sql)
    }
  }

  /**
   * Acquires a database transaction and performs given action
   *
   * @param block Operations to perform on database
   *
   * @tparam T    Type of return value
   *
   * @return      Whatever block returns
   */
  def withTransaction[T](block: Connection => T): T = {
    DB.withTransaction {
      implicit connection =>
        try {
          block(connection)
        } catch {
          case t: Throwable =>
            Log.error("Database transaction failed, rolling back!..", "Database.withTransaction")
            connection.rollback()

            throw t
        }
    }
  }
}
