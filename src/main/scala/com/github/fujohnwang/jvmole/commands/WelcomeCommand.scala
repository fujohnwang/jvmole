package com.github.fujohnwang.jvmole.commands

import sbt.Command

trait WelcomeCommand {
  def welcome = Command.command("welcome") {
    state => state.log.info("welcome to jvmole's world~"); state
  }
}