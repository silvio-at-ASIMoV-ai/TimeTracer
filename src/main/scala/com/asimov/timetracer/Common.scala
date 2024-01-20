package com.asimov.timetracer

import java.awt.event.{ActionEvent, KeyEvent}
import javax.swing.{AbstractAction, JButton, JComponent, JOptionPane, JRootPane, KeyStroke}
import scala.swing.*

val arial12 = new Font("Arial", 0, 12)  // sans-serif  Arial
val arial22 = new Font("Arial", 0, 22)
val arial45 = new Font("Arial", 0, 45)
val logoColor = new Color(162, 186, 249, 255)
val reminderColor = new Color(19, 63, 213, 255)    //$00D53F13   19,63,213

private class MyButton(caption: String) extends Button {
  text = caption
  font = arial12
  preferredSize = new Dimension(88, 33)
  minimumSize = preferredSize
  maximumSize = preferredSize
}

class MyLabel(caption: String) extends Label {
  text = caption
  font = arial12
}

def setCancelButton(rp: JRootPane, b: JButton): Unit = {
  rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel")
  rp.getActionMap.put("cancel", new AbstractAction() {
    def actionPerformed(ev: ActionEvent): Unit = {
      b.doClick()
    }
  })
}

def showMessage(parent: java.awt.Component, msg: String, title: String, error: Boolean): Unit = {
  if (error) JOptionPane.showMessageDialog(parent, msg, title, JOptionPane.ERROR_MESSAGE)
  else JOptionPane.showMessageDialog(parent, msg, title, JOptionPane.INFORMATION_MESSAGE)
}



