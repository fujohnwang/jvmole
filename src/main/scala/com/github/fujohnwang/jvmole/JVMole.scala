package com.github.fujohnwang.jvmole

import sbt._
import complete._
import DefaultParsers._
import java.io.File

final class JVMole extends xsbti.AppMain {

  val initialLogging = initialGlobalLogging


  def run(configuration: xsbti.AppConfiguration): xsbti.MainResult = {
    if (configuration.arguments().length != 1) {
      initialLogging.backed.warn("welcome to jvmole's world~")
      // TODO attach to target jvm if pid is available
    }
    MainLoop.runLogged(initialState(configuration))
  }

  /**
   * initial commands:  say hello -> attach [pid] -> shell
   * @param configuration
   * @return
   */
  def initialState(configuration: xsbti.AppConfiguration): State = {
    val commandDefinitions = hello +: BasicCommands.allBasicCommands
    val commandsToRun = Seq("welcome", "shell")
    State(configuration, commandDefinitions, Set.empty, None, commandsToRun, State.newHistory, AttributeMap.empty, initialLogging, State.Continue)
  }

  def welcome = Command.command("welcome", "", "")(state => {
    state.log.info("welcome to jvmole's world~")
    state
  })

  def hello = Command("hello", ("hello <name>", "just say hello"), "just say hello")(state => helloParser)(helloAction)

  def helloParser: Parser[String] = token(Space) ~> token(NotSpace, "<name>")

  def helloAction(state: State, name: String): State = {
    state.log.info("Hello %s!".format(name))
    state
  }

  def initialGlobalLogging: GlobalLogging = {
    var logFile = new File("jvmole.log")
    if (!logFile.exists()) {
      if (!logFile.createNewFile()) logFile = File.createTempFile("jvmole", "log")
    }
    GlobalLogging.initial(MainLogging.globalDefault _, logFile)
  }

  def exitHooks(): Set[ExitHook] = {
    Set.empty + ExitHook(() => println("just a exit hook"))
  }
}
