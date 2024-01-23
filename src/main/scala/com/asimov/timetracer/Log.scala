package com.asimov.timetracer

import java.text.SimpleDateFormat
import java.util.Date

object Log {
  def apply(query: String, previousState: String): Unit = {
    MySQL.connection match {
      case Some(conn) =>
        val timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())
        val logQuery = "INSERT INTO `TimeTracer`.`Log` (`Query`, `PreviousState`, `Timestamp`) VALUES (?, ?, ?)"
        val statement = conn.prepareStatement(logQuery)
        statement.setString(1, query)
        statement.setString(2, previousState)
        statement.setString(3, timestamp)
        statement.executeUpdate()
      case _ =>
    }
  }
}
