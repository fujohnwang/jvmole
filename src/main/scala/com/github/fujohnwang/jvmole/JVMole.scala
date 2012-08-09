package com.github.fujohnwang.jvmole

import commands._
import sbt._
import java.io.File
import xsbti.AppConfiguration

final class JVMole extends xsbti.AppMain with ProjectInfo with Commands {
  val initialLogging = initialGlobalLogging

  def run(configuration: xsbti.AppConfiguration): xsbti.MainResult = {
    MainLoop.runLogged(initialState(configuration))
  }

  def initialState(configuration: xsbti.AppConfiguration): State = {
    val commandDefinitions = listMBeans +: execMBeanMethod +: beanDesc +: setAttr +: listVirtualMachines +: attach +: detach +: welcome +: BasicCommands.allBasicCommands
    val commandsToRun = Seq("welcome", "iflast shell")
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


object JVMole {
  def main(args: Array[String]) {
    new JVMole().run(new AppConfiguration() {
      def arguments() = args

      def baseDirectory() = null

      def provider() = null
    })
  }
}