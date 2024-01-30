package com.asimov.timetracer.ui

import com.asimov.timetracer.{MyButton, MySQL}

import scala.swing.Orientation.Vertical
import scala.swing.event.ButtonClicked
import scala.swing.{BoxPanel, Button, Dialog}

object ReportsUI extends Dialog {
  override def closeOperation(): Unit = System.exit(0)

  def apply(): Unit = show()
  private def show(): Unit = visible = true

  private def hide(): Unit = visible = false

  private val closeBtn: Button = new MyButton("Close")
  private val reportsPanel: BoxPanel = new BoxPanel(Vertical)  {
    contents += closeBtn
  }

  private val db = MySQL
  title = "Time Tracer - Reports"
  contents = reportsPanel
  listenTo(closeBtn)
  reactions += {
    case ButtonClicked(`closeBtn`) => System.exit(0)
  }
  pack()
  centerOnScreen()
}
