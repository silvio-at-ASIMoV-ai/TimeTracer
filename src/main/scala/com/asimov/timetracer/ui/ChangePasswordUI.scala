package com.asimov.timetracer.ui

import com.asimov.timetracer.Password.generateHash
import com.asimov.timetracer.ui.LoginUI.login
import com.asimov.timetracer.{MyButton, MyLabel, MySQL, setCancelButton, showMessage}

import scala.swing.Orientation.Vertical
import scala.swing.event.ButtonClicked
import scala.swing.{BoxPanel, Button, Dialog, PasswordField}

object ChangePasswordUI extends Dialog {
  var roleID: Int = 0
  var employeeID: Int = 0
  var firstLogin: Boolean = false
  private val ok: Button = new MyButton("OK")
  private val cancel: Button = new MyButton("Cancel")

  override def closeOperation(): Unit = System.exit(0)

  def show(): Unit = visible = true

  def hide(): Unit = {
    clear()
    visible = false
  }

  def apply(userName: String, roleId: Int, employeeId: Int, firstlogin: Boolean): Unit = {
    this.userName.text = userName
    roleID = roleId
    employeeID = employeeId
    firstLogin = firstlogin
    oldPwd.enabled = !firstlogin
    val focusTraversalList = if(firstLogin) List(oldPwd.peer, newPwd.peer, verPwd.peer, ok.peer, cancel.peer)
    else List(newPwd.peer, verPwd.peer, ok.peer, cancel.peer)
    if(firstLogin) newPwd.requestFocus() else oldPwd.requestFocus()
    peer.setFocusTraversalPolicy(new ListFocusTraversalPolicy(focusTraversalList))
    show()
  }

  private def goBack(): Unit = {
    roleID match {
      case 1 =>
        hide()
        AdminUI()
      case _ =>
        hide()
        PunchInUI(userName.text, employeeID, roleID)
    }
  }

  private def clear(): Unit = {
    oldPwd.peer.setText("")
    newPwd.peer.setText("")
    verPwd.peer.setText("")
  }

  private def change(): Unit = {
    def doChange(): Unit = {
      if (newPwd.password.mkString == verPwd.password.mkString) {
        val chPwd = generateHash(userName.text, newPwd.password.mkString)
        val db = MySQL
        if (db.connection.nonEmpty) {
          val statement = db.connection.get.prepareStatement("UPDATE TimeTracer.Users SET Password = ? WHERE UserName = ?")
          statement.setString(1, chPwd)
          statement.setString(2, userName.text)
          if (statement.executeUpdate() > 0) {
            showMessage(this.peer, "Password Successfully Changed", "Change Password", false)
            goBack()
          } else {
            showMessage(this.peer, "Error Changing Password", "Change Password", true)
            goBack()
          }
        } else {
          showMessage(this.peer, "Error Changing Password", "Change Password", true)
          goBack()
        }
      } else {
        showMessage(this.peer, "Passwords don't match", "Change Password", true)
        clear()
      }
    }

    if (!firstLogin) {
      login(userName.text, oldPwd.password.mkString) match {
        case Left(value) if value._1 =>
          doChange()
        case _ =>
          showMessage(this.peer, "Wrong Password", "Change Password", true)
          clear()
      }
    } else {
      doChange()
    }
  }

  val userName = new MyLabel("")
  private val oldPwd = new PasswordField()
  private val newPwd = new PasswordField()
  private val verPwd = new PasswordField()

  private val chPwdPanel: BoxPanel = new BoxPanel(Vertical) {
    contents += pwdPanel
    contents += buttonPanel
  }

  private def pwdPanel: MyMigPanel = new MyMigPanel {
    colConstraints = "[68px][220px][20px]"
    rowConstraints = "[35px][35px][35px][35px]"
    add(new MyLabel("User Name:"), "cell 0 0, alignx right")
    add(userName, "cell 1 0, alignx left")
    add(new MyLabel("Old Password:"), "cell 0 1, alignx right")
    add(oldPwd, "cell 1 1, growx")
    add(new MyLabel("New Password:"), "cell 0 2, alignx right")
    add(newPwd, "cell 1 2, growx")
    add(new MyLabel("Verify Password:"), "cell 0 3, alignx right")
    add(verPwd, "cell 1 3, growx")
  }

  private def buttonPanel: MyMigPanel = new MyMigPanel {
    colConstraints = "[150px][68px][68px]"
    rowConstraints = "[35px]"
    add(ok, "cell 1 0")
    add(cancel, "cell 2 0")
  }

  title = "Time Tracer - Change Password"
  contents = chPwdPanel
  peer.getRootPane.setDefaultButton(ok.peer)
  setCancelButton(peer.getRootPane, cancel.peer)
  listenTo(ok, cancel)
  reactions += {
    case ButtonClicked(`ok`) => change()
    case ButtonClicked(`cancel`) => if(firstLogin) System.exit(0) else goBack()
  }
  pack()
  centerOnScreen()
}
