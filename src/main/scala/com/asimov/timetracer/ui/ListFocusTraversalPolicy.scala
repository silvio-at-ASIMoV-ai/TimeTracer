package com.asimov.timetracer.ui

import java.awt.{FocusTraversalPolicy, Container, Component}

class ListFocusTraversalPolicy(compList: List[Component]) extends FocusTraversalPolicy {
  override def getComponentAfter(aContainer: Container, aComponent: Component): Component = {
    val index = compList.indexOf(aComponent)
    if(index == compList.size - 1) compList.head else compList(index + 1)
  }

  override def getComponentBefore(aContainer: Container, aComponent: Component): Component = {
    val index = compList.indexOf(aComponent)
    if (index <= 0) compList.last else compList(index - 1)
  }

  override def getFirstComponent(aContainer: Container): Component = compList.head

  override def getLastComponent(aContainer: Container): Component = compList.last

  override def getDefaultComponent(aContainer: Container): Component = compList.head

}
