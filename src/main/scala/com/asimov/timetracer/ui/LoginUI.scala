package com.asimov.timetracer.ui

import com.asimov.timetracer.{MySQL, Password, arial12, arial45, logoColor, setCancelButton, showMessage}

import java.awt
import javax.swing.{ImageIcon, JComponent}
import scala.swing.*
import scala.swing.Orientation.Vertical
import scala.swing.event.ButtonClicked

object LoginUI extends Dialog {

  override def closeOperation(): Unit = System.exit(0)

  private val loginBtn: Button = new Button() {
    text = "Login"
    font = arial12
    preferredSize = new Dimension(88, 33)
  }

  private val cancelBtn: Button = new Button() {
    text = "Cancel"
    font = arial12
    preferredSize = new Dimension(88, 33)
  }

  private val userNameLabel = new Label("User Name:") {
    font = arial12
  }

  private val passwordLabel = new Label("Password:") {
    font = arial12
  }
  private val userName = new TextField() {
    preferredSize = new Dimension(165, 23)
  }
  private val password = new PasswordField() {
    preferredSize = new Dimension(165, 23)
  }

  private val timeTracer = new Label("Time Tracer") {
    font = arial45
    foreground = logoColor
  }

  private val logo = new Label() {
    icon = new ImageIcon(getClass.getResource("/logo.png"))
  }

  private lazy val loginPanel: BoxPanel = new BoxPanel(Vertical) {
    contents += logoPanel
    contents += inputPanel
  }

  private lazy val inputPanel: MyMigPanel = new MyMigPanel {
    colConstraints = "[68px][180px][16px][95px]"
    rowConstraints = "[33px][5px][33px][12px]"
    add(userNameLabel, "cell 0 0, gapx 10px, aligny center, alignx right")
    add(passwordLabel, "cell 0 2, gapx 10px, aligny center, alignx right")
    add(userName, "cell 1 0, gapx 10px, aligny center")
    add(password, "cell 1 2, gapx 10px, aligny center")
    add(loginBtn, "cell 3 0, gapx 10px, growx, aligny center")
    add(cancelBtn, "cell 3 2, gapx 10px, growx, aligny center")
  }

  private lazy val logoPanel: MyMigPanel = new MyMigPanel {
    colConstraints = "[5px][80px][5px][276px]"
    rowConstraints = "[50px]"
    add(logo, "cell 1 0")
    add(timeTracer, "cell 3 0")
  }


  def login(userName: String, password: String): Either[(Boolean, String, Int, Int), Either[(String, Int, Int), String]] = {
    val db = MySQL
    if (db.connection.nonEmpty) {
      val statement = db.connection.get.prepareStatement("SELECT * FROM TimeTracer.Users WHERE UserName = ?")
      statement.setString(1, userName)
      val resultSet = statement.executeQuery()
      if (resultSet.next()) {
        val dbUserName = resultSet.getString(1)
        val dbPassword = resultSet.getString(2)
        val roleId = resultSet.getInt(3)
        val employeeId = resultSet.getInt(4)
        if (dbPassword == null) Right(Left(dbUserName, roleId, employeeId)) // new user
        else Left((Password.verify(dbUserName, password, dbPassword), dbUserName, roleId, employeeId))
      } else {
        Right(Right("Unknown User"))
      }
    } else {
      Right(Right("No connection to DataBase"))
    }
  }

  private def doLogin(): Unit = {
    if(password.password.isEmpty) {
      showMessage(peer, "Password cannot be empty",
        "No Empty Password", true)
      password.requestFocus()
    } else {
      login(userName.text, password.password.mkString) match {
        case Left(t) => if (t._1) loginSuccess(t._2, t._3, t._4) else
          showMessage(peer, "Access Denied", "Access Denied", true)
          password.peer.setText("")
        case Right(e) => e match
          case Left(username, roleId, employeeId) => firstLogin(username, roleId, employeeId)
          case Right(str) => showMessage(peer, str, "Error", true)
      }
    }
  }

  def firstLogin(user: String, roleId: Int, employeeId: Int): Unit = {
    hide()
    ChangePasswordUI(user, roleId, employeeId, true)
  }

  private def loginSuccess(user: String, roleId: Int, employeeId:Int): Unit = {
    roleId match {
      case 1 => hide()
        AdminUI()
      case _ => hide()
        PunchInUI(user, employeeId, roleId)
    }
  }

  def show(): Unit = {
    visible = true
  }
  def hide(): Unit = {
    visible = false
  }

  def apply(): Unit = show()


  title = "Time Tracer - Login"
  contents = loginPanel
  peer.getRootPane.setDefaultButton(loginBtn.peer)
  setCancelButton(peer.getRootPane, cancelBtn.peer)
  private val focusTraversalList = List(userName.peer, password.peer, loginBtn.peer, cancelBtn.peer)
  peer.setFocusTraversalPolicy(new ListFocusTraversalPolicy(focusTraversalList))
  listenTo(loginBtn, cancelBtn)
  reactions += {
    case ButtonClicked(`loginBtn`) => doLogin()
    case ButtonClicked(`cancelBtn`) => System.exit(0)
  }
  pack()
  centerOnScreen()
  userName.requestFocus()
}
