package com.asimov.timetracer.ui

import com.asimov.timetracer.{arial22, MyButton, reminderColor, setCancelButton}

import java.awt.Toolkit
import scala.swing.event.ButtonClicked
import scala.swing.{Button, Dialog, Dimension, Label}

object ReminderUI extends Dialog {
  private val punchOut: Button = new MyButton("Punch Out") {
    preferredSize = new Dimension(95, 33)
    minimumSize = preferredSize
    maximumSize = preferredSize
  }
  private val close: Button = new MyButton("Close") {
    preferredSize = new Dimension(95, 33)
    minimumSize = preferredSize
    maximumSize = preferredSize
  }
  private val timeLbl: Label = new Label() {
    font = arial22
    foreground = reminderColor
  }
  private val projectLbl: Label = new Label() {
    font = arial22
    foreground = reminderColor
  }

  override def closeOperation(): Unit = System.exit(0)

  def show(): Unit = visible = true

  def hide(): Unit = visible = false

  def apply(time: String, project: String): Unit = {
    timeLbl.text = time
    projectLbl.text = project
    show()
  }

  private lazy val remindPanel: MyMigPanel = new MyMigPanel {
    colConstraints = "[5px][250px][95px][5px]"
    rowConstraints = "[5px][33px][5px][33px][5px]"
    add(timeLbl, "cell 1 1, alignx left")
    add(projectLbl, "cell 1 3, alignx left")
    add(punchOut, "cell 2 1, growx")
    add(close, "cell 2 3, growx")
  }


  title = "Time Tracer - Reminder"
  contents = remindPanel
  peer.getRootPane.setDefaultButton(punchOut.peer)
  setCancelButton(peer.getRootPane, close.peer)
  listenTo(punchOut, close)
  reactions += {
    case ButtonClicked(`punchOut`) => hide()
      PunchInUI.show()
    case ButtonClicked(`close`) => System.exit(0)
  }
  pack()
  peer.setLocation(Toolkit.getDefaultToolkit.getScreenSize.getWidth.toInt - peer.getWidth - 25, 25)
}
