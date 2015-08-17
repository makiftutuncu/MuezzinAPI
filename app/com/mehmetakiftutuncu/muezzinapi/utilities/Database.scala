package com.mehmetakiftutuncu.muezzinapi.utilities

import anorm.{Row, SimpleSql}
import play.api.Play.current
import play.api.db.DB

/**
 * A utility object to allow database operations
 */
object Database {
  /**
   * Performs an apply on database
   *
   * @param sql Query to apply
   *
   * @return A list of [[anorm.Row]]s as the result of query
   */
  def apply(sql: SimpleSql[Row]): List[Row] = {
    DB.withConnection {
      implicit connection =>
        sql.withQueryTimeout(Option(Conf.dbTimeout)).apply().toList
    }
  }

  /**
   * Performs an update on database
   *
   * @param sql Query to execute
   *
   * @return Number of affected rows by the update
   */
  def executeUpdate(sql: SimpleSql[Row]): Int = {
    DB.withConnection {
      implicit connection =>
        sql.withQueryTimeout(Option(Conf.dbTimeout)).executeUpdate()
    }
  }
}
