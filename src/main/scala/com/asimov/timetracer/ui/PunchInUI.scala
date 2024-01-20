package com.asimov.timetracer.ui

import com.asimov.timetracer.{MySQL, MyButton, MyLabel, setCancelButton}

import java.awt.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.sql.Timestamp
import javax.swing.border.LineBorder
import scala.swing.Orientation.Vertical
import scala.swing.*
import scala.swing.event.ButtonClicked

object PunchInUI extends Dialog {
  private val db = MySQL
  private var userName: String = ""
  private var employeeID: Int = 0
  private var roleID: Int = 0
  private val formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
  private val projects = getProjects

  private val ok: Button = new MyButton("OK")
  private val accept: Button = new MyButton("Accept")
  private val cancel: Button = new MyButton("Cancel")
  private val reminder: Button = new MyButton("Remind")
  private val changePwd: Button = new MyButton("Ch. Pwd")

  private lazy val rightPanel: MyMigPanel = new MyMigPanel {
    colConstraints = "[68px]"
    rowConstraints = "[35px][35px][35px][35px][35px]"
    add(ok, "cell 0 0")
    add(accept, "cell 0 1")
    add(cancel, "cell 0 2")
    add(reminder, "cell 0 3")
    add(changePwd, "cell 0 4")
  }

  private val employee = new MyLabel("")
  private val time = new MyLabel(formatter.format(new Date()))
  private lazy val reviewPanel: MyMigPanel = new MyMigPanel {
    colConstraints = "[70px][5px][70px]"
    rowConstraints = "[20px][20px]"
    add(new MyLabel("Employee:"), "cell 0 0, alignx right")
    add(new MyLabel("Time:"), "cell 0 1, alignx right")
    add(employee, "cell 2 0")
    add(time, "cell 2 1")
  }

  private val projectsLV: ListView[String] = new ListView()
  projectsLV.border = new LineBorder(Color.black)
  projectsLV.listData = projects.map(_._2)

  private val leftPanel: MyMigPanel = new MyMigPanel {
    colConstraints = "[115px]"
    rowConstraints = "[20px][163px]"
    add(new MyLabel("Projects"), "cell 0 0")
    add(projectsLV, "cell 0 1, growx, growy")
  }

  private val in = new RadioButton("IN")
  private val out = new RadioButton("OUT")
  private val inOut = new ButtonGroup()
  inOut.buttons += in
  inOut.buttons += out

  private lazy val midPanel: MyMigPanel = new MyMigPanel {
    colConstraints = "[70px]"
    rowConstraints = "[40px][20px][20px][20px][59px]"
    add(reviewPanel, "cell 0 0")
    add(in, "cell 0 2")
    add(out, "cell 0 3")
  }

  private lazy val lmrPanel: FlowPanel = new FlowPanel() {
    contents += leftPanel
    contents += midPanel
    contents += rightPanel
  }

  private val lastPunchedIn = new MyLabel("")
  private val lastPunchedInPanel: FlowPanel = new FlowPanel(FlowPanel.Alignment.Left)() {
    contents += new MyLabel("Punched in at")
    contents += lastPunchedIn
  }

  private val punchInPanel: BoxPanel = new BoxPanel(Vertical) {
    contents += lmrPanel
    contents += lastPunchedInPanel
  }

  override def closeOperation(): Unit = System.exit(0)

  def show(): Unit = visible = true

  def hide(): Unit = visible = false

  def apply(username: String, employeeId: Int, roleId: Int): Unit = {
    val timeOut = new javax.swing.AbstractAction() {
      def actionPerformed(e: java.awt.event.ActionEvent): Unit = {
        time.text = formatter.format(new Date())
      }
    }
    val t = new javax.swing.Timer(1000, timeOut)
    t.start()
    userName = username
    employeeID = employeeId
    roleID = roleId
    employee.text = getEmployeeName
    val lastTimeData = getLastTimeData
    if(lastTimeData._1 != null) {
      out.selected = lastTimeData._2
      in.selected = !lastTimeData._2
      projectsLV.selectIndices(projects.indexOf(projects.filter(_._1 == lastTimeData._3).head))
      lastPunchedIn.text = formatter.format(lastTimeData._1)
    }
    show()
  }

  private def getLastTimeData: (Date, Boolean, Int) = {
    if (db.connection.nonEmpty) {
      val statement = db.connection.get.prepareStatement(
        "SELECT * FROM TimeTracer.Times WHERE EmployeeID = ? ORDER BY PunchedTime DESC")
      statement.setInt(1, employeeID)
      val resultSet = statement.executeQuery()
      if (resultSet.next()) {
        (resultSet.getTimestamp(4), resultSet.getBoolean(5), resultSet.getInt(3))
      } else {
        (null,false,0)
      }
    } else {
      (null,false,0)
    }
  }

  private def getEmployeeName: String = {
    if (db.connection.nonEmpty) {
      val statement = db.connection.get.prepareStatement("SELECT * FROM TimeTracer.Employees WHERE IDEmployee = ?")
      statement.setInt(1, employeeID)
      val resultSet = statement.executeQuery()
      if (resultSet.next()) {
        s"${resultSet.getString(2)} ${resultSet.getString(3)}"
      } else {
        "ResultSet empty"
      }
    } else {
      "No Connection"
    }
  }

  private def getProjects: List[(Int,String)] = {
    if (db.connection.nonEmpty) {
      val statement = db.connection.get.prepareStatement("SELECT * FROM TimeTracer.Projects")
      val rs = statement.executeQuery()
      Iterator.from(0).takeWhile(_ => rs.next())
        .map(_ => (rs.getInt(1), rs.getString(2)))
        .toList
    } else {
      Nil
    }
  }

  private def punchIn(): Unit = {
    if (db.connection.nonEmpty) {
      val statement = db.connection.get.prepareStatement("INSERT INTO TimeTracer.Times " +
        "(`EmployeeID`, `ProjectID`, `PunchedTime`, `In`, `InsertUser`, `InsertTimestamp`) VALUES (?, ?, ?, ?, ?, ?)")
      val timeStamp: Timestamp = new Timestamp(System.currentTimeMillis())
      val projId = projects(projectsLV.peer.getSelectedIndex)._1
      statement.setInt(1, employeeID)
      statement.setInt(2, projId)
      statement.setTimestamp(3, timeStamp)
      statement.setBoolean(4, in.selected)
      statement.setString(5, userName)
      statement.setTimestamp(6, timeStamp)
      statement.execute()
    }
  }

  private def remind(): Unit = {
    val(t, i, p) = getLastTimeData
    hide()
    ReminderUI(formatter.format(t), projects.filter(_._1 == p).head._2)
  }

  title = "Time Tracer - Punch In"
  contents = punchInPanel
  peer.getRootPane.setDefaultButton(ok.peer)
  setCancelButton(peer.getRootPane, cancel.peer)
  listenTo(ok, cancel, accept, reminder, changePwd)
  reactions += {
    case ButtonClicked(`ok`) => punchIn()
      System.exit(0)
    case ButtonClicked(`cancel`) => System.exit(0)
    case ButtonClicked(`accept`) => punchIn()
    case ButtonClicked(`reminder`) => remind()
    case ButtonClicked(`changePwd`) => hide()
      ChangePasswordUI(userName, roleID, employeeID, false)
  }
  pack()
  centerOnScreen()

}
