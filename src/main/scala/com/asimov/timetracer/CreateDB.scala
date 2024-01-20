package com.asimov.timetracer

import com.asimov.timetracer.ui.{ListFocusTraversalPolicy, LoginUI}

import java.io.{File, PrintWriter, FileNotFoundException}
import java.sql.{Connection, DriverManager, SQLException}
import scala.io.Source
import scala.swing.Orientation.Vertical
import scala.swing.event.ButtonClicked
import scala.swing.{BoxPanel, Button, Dialog, Dimension, FlowPanel, PasswordField, TextField}

object CreateDB extends Dialog {
  override def closeOperation(): Unit = System.exit(0)

  private val okBtn: Button = new Button() {
    text = "Create"
    font = arial12
    preferredSize = new Dimension(88, 33)
  }
  private val cancelBtn: Button = new Button() {
    text = "Cancel"
    font = arial12
    preferredSize = new Dimension(88, 33)
  }
  private val buttonPanel: FlowPanel = new FlowPanel(FlowPanel.Alignment.Center)() {
    contents += okBtn
    contents += cancelBtn
  }

  private val url = new TextField() {
    preferredSize = new Dimension(300, 30)
    text = "jdbc:mysql://<server-address>:3306/mysql"
  }
  private val urlPanel: FlowPanel = new FlowPanel(FlowPanel.Alignment.Right)() {
    contents += MyLabel("URL:")
    contents += url
  }

  private val driver = new TextField() {
    preferredSize = new Dimension(300, 30)
    text = "com.mysql.cj.jdbc.Driver"
  }
  private val driverPanel: FlowPanel = new FlowPanel(FlowPanel.Alignment.Right)() {
    contents += MyLabel("Driver:")
    contents += driver
  }

  private val userName = new TextField() {
    preferredSize = new Dimension(300, 30)
  }
  private val userPanel: FlowPanel = new FlowPanel(FlowPanel.Alignment.Right)() {
    contents += MyLabel("User Name:")
    contents += userName
  }

  private val pwd = new PasswordField() {
    preferredSize = new Dimension(300, 30)
  }
  private val pwdPanel: FlowPanel = new FlowPanel(FlowPanel.Alignment.Right)() {
    contents += MyLabel("Password:")
    contents += pwd
  }

  private val createDBPanel: BoxPanel = new BoxPanel(Vertical) {
    contents += urlPanel
    contents += driverPanel
    contents += userPanel
    contents += pwdPanel
    contents += buttonPanel
  }

  private def checkConnection(url: String, driver: String, username: String, password: String): Option[Connection] = {
    try {
      Class.forName(driver)
      val conn = DriverManager.getConnection(url, username, password)
      Some(conn)
    } catch {
      case e: Exception =>
        println(e.printStackTrace())
        None
    }
  }

  private def doCreateDB(): Unit = {
    checkConnection(url.text, driver.text, userName.text, pwd.password.mkString) match {
      case Some(connection) =>
        try {
          // create DB and tables
          val statement = connection.createStatement()
          val createQueryResFile = Source.fromResource("CreateTables.sql")
          val createQuery = createQueryResFile.getLines.mkString.split(";")
          createQuery.foreach(statement.addBatch)
          val results = statement.executeBatch()
          if(!connection.getAutoCommit) connection.commit()
          // create ini file
          val iniFileName = s"${System.getProperty("user.home")}${File.separatorChar}" +
            s".TimeTracer${File.separatorChar}TimeTracer.ini"
          val iniFile = new File(iniFileName)
          val pw = new PrintWriter(iniFile)
          val sep = System.lineSeparator()
          pw.write(s"url=${url.text}$sep")
          pw.write(s"driver=${driver.text}$sep")
          pw.write(s"username=${userName.text}$sep")
          pw.write(s"password=${pwd.password.mkString}$sep")
          pw.close()
          // hide this dialog and go on to login
          hide()
          LoginUI()
        } catch {
          case notFound: FileNotFoundException =>
            showMessage(peer, "Cannot find the database creation file",
            "Cannot Create Database", true)
          case sqlException: SQLException =>
            println(sqlException.printStackTrace())
            showMessage(peer, "Problems with Database creation",
            "Cannot Create Database", true)
        }
      case None => showMessage(peer, "Cannot connect, review the parameters", "Cannot Connect", true)
    }
  }

  def show(): Unit = visible = true

  def hide(): Unit = visible = false

  def apply(): Unit = show()

  title = "Time Tracer - Create Database"
  contents = createDBPanel
  peer.getRootPane.setDefaultButton(okBtn.peer)
  setCancelButton(peer.getRootPane, cancelBtn.peer)
  private val focusTraversalList = List(url.peer, driver.peer, userName.peer, pwd.peer, okBtn.peer, cancelBtn.peer)
  peer.setFocusTraversalPolicy(new ListFocusTraversalPolicy(focusTraversalList))
  listenTo(okBtn, cancelBtn)
  reactions += {
    case ButtonClicked(`okBtn`) => doCreateDB()
    case ButtonClicked(`cancelBtn`) => System.exit(0)
  }
  pack()
  centerOnScreen()
  url.requestFocus()
}
