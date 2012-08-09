package com.github.fujohnwang.jvmole

import sbt._
import complete._
import DefaultParsers._
import java.io.File

final class JVMole extends xsbti.AppMain {

  def run(configuration: xsbti.AppConfiguration): xsbti.MainResult = {
    if (configuration.arguments().length == 1) {
      println("welcome to jvmole's world: " + configuration.arguments().head)
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
    val commandsToRun = Seq("shell", "hello")
    State(configuration, commandDefinitions, Set.empty, None, commandsToRun, State.newHistory, AttributeMap.empty, initialGlobalLogging, State.Continue)
  }

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
