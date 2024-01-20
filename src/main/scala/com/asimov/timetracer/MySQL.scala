package com.asimov.timetracer

import java.io.File
import java.sql.{Connection, DriverManager}
import scala.io.Source

case class ConnectionParams(url: String, driver: String, username: String, password: String)

object MySQL {

  lazy val connection: Option[Connection] = getConnection

  private def getParamsFromIniFile: Option[ConnectionParams] = {
    val filename = s"${System.getProperty("user.home")}${File.separatorChar}.TimeTracer${File.separatorChar}TimeTracer.ini"
    val f = new File(filename)
    if(f.exists()) {
      val s = Source.fromFile(filename)
      val lines = s.getLines()
      val params = lines.map { data =>
        val spl = data.split("=")
        spl.head -> spl.last
      }.toMap
      s.close()
      Some(ConnectionParams(params("url"), params("driver"), params("username"), params("password")))
    } else None
  }

  private def getConnection: Option[Connection] = {
    getParamsFromIniFile match {
      case Some(p: ConnectionParams) =>
        try {
          Class.forName(p.driver)
          Some(DriverManager.getConnection(p.url, p.username, p.password))
        } catch {
          case e: Exception =>
            println(e.getLocalizedMessage)
            None
        }
      case None =>
        showMessage(null, "Cannot find Ini File", "Ini File missing", true)
        None
    }
  }
}
