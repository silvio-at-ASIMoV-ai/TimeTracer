package com.asimov.timetracer

import io.github.nremond.PBKDF2

import scala.annotation.nowarn

object Password {

  @nowarn("cat=deprecation")
  private def convertBytesToHex(bytes: Seq[Byte]): String = {
    bytes.map(b => String.format("%02X", Byte.box(b))).mkString
  }

  def generateHash(username: String, password: String): String = {
    convertBytesToHex(PBKDF2(password.getBytes, username.getBytes, 450000, 49))
  }

  def verify(username: String, password: String, hash: String): Boolean = {
    hash == generateHash(username, password)
  }
  
}