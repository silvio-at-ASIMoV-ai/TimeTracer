package com.asimov.timetracer.ui

import net.miginfocom.swing.MigLayout
import scala.swing.{Component, LayoutContainer, Panel}

class MyMigPanel extends Panel with LayoutContainer {

  override lazy val peer = new javax.swing.JPanel(new MigLayout) with SuperMixin

  private def layoutManager: MigLayout = peer.getLayout.asInstanceOf[MigLayout]

  override type Constraints = String

  override protected def constraintsFor(c: Component): String = new Constraints()

  override protected def areValid(c: String): (Boolean, String) = (true, "")

  override protected def add(comp: Component, c: String): Unit = peer.add(comp.peer, c)

  def colConstraints_= (c: String):Unit = layoutManager.setColumnConstraints(c)

  def colConstraints:AnyRef = layoutManager.getColumnConstraints

  def rowConstraints_= (r: String):Unit = layoutManager.setRowConstraints(r)

  def rowConstraints:AnyRef = layoutManager.getRowConstraints

}
