package com.github.fujohnwang.jvmole.commands

import sbt.Command
import com.sun.tools.attach.VirtualMachine
import com.github.fujohnwang.jvmole.JVMoleHeadQuarter
import sbt.complete.Parser
import sbt.complete.DefaultParsers._

/**
 * attach to specific JVM process as per pid.
 */
trait AttachCommand {
  val CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress"

  def attach = Command("attach", ("attach <pid>", "attach to specific virtual machine instance as per pid"), "")(_ => token(Space) ~> token(Digit.+.map(_.mkString), "<pid>"))((s, pid) => {
    JVMoleHeadQuarter.virtualMachine = Some(VirtualMachine.attach(pid))
    s
  })
}