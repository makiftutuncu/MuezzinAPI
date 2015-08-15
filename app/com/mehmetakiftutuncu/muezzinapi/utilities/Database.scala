package com.mehmetakiftutuncu.muezzinapi.utilities

import anorm.{Row, SimpleSql}
import play.api.Play.current
import play.api.db.DB

/**
 * Created by akif on 15/08/15.
 */
object Database {
  def apply(sql: SimpleSql[Row]): List[Row] = {
    DB.withConnection {
      implicit connection =>
        sql.withQueryTimeout(Option(Conf.dbTimeout)).apply().toList
    }
  }

  def executeUpdate(sql: SimpleSql[Row]): Int = {
    DB.withConnection {
      implicit connection =>
        sql.withQueryTimeout(Option(Conf.dbTimeout)).executeUpdate()
    }
  }
}
