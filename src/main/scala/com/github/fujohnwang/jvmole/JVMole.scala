package com.github.fujohnwang.jvmole

import commands.{Hello, Welcome}
import sbt._
import java.io.File

final class JVMole extends xsbti.AppMain with Welcome with Hello {
  val initialLogging = initialGlobalLogging

  def run(configuration: xsbti.AppConfiguration): xsbti.MainResult = {
    if (configuration.arguments().length != 1) {
      initialLogging.backed.warn("welcome to jvmole's world~")
      // TODO attach to target jvm if pid is available
    }
    MainLoop.runLogged(initialState(configuration))
  }

  def initialState(configuration: xsbti.AppConfiguration): State = {
    val commandDefinitions = hello +: BasicCommands.allBasicCommands
    val commandsToRun = Seq("welcome", "shell")
    State(configuration, commandDefinitions, Set.empty, None, commandsToRun, State.newHistory, AttributeMap.empty, initialLogging, State.Continue)
  }

  def initialGlobalLogging: GlobalLogging = {
    var logFile = new File("jvmole.log")
    if (!logFile.exists()) {
      if (!logFile.createNewFile()) logFile = File.createTempFile("jvmole", "log")
    }
    GlobalLogging.initial(MainLogging.globalDefault _, logFile)
  }
}
