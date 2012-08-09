package com.github.fujohnwang.jvmole.commands

import sbt.{State, Command}
import sbt.complete.Parser
import sbt.complete.DefaultParsers._

trait Hello {
  def hello = Command("hello", ("hello <name>", "just say hello"), "just say hello")(state => helloParser)(helloAction)

  def helloParser: Parser[String] = token(Space) ~> token(NotSpace, "<name>")

  def helloAction(state: State, name: String): State = {
    state.log.info("Hello %s!".format(name))
    state
  }
}