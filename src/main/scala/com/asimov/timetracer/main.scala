package com.asimov.timetracer

import com.asimov.timetracer.ui.LoginUI

import javax.swing.UIManager
import com.formdev.flatlaf.FlatIntelliJLaf

import java.io.File

@main
def main(): Unit = {
  UIManager.setLookAndFeel(new FlatIntelliJLaf())
  val filename = s"${System.getProperty("user.home")}${File.separatorChar}.TimeTracer${File.separatorChar}TimeTracer.ini"
  val f = new File(filename)
  if (f.exists()) {
    LoginUI()
  } else {
    CreateDB()
  }

}

