package com.github.fujohnwang.jvmole.commands

import sbt.Command
import com.github.fujohnwang.jvmole.JVMoleHeadQuarter

trait DetachCommand {
  def detach = Command.command("detach", "detach from attached JVM instance if any", "")(s => {
    JVMoleHeadQuarter.virtualMachine match {
      case None => s.log.warn("no attached JVM instance found when trying to detach it.")
      case Some(vm) => vm.detach(); JVMoleHeadQuarter.virtualMachine = None
    }
    s
  })
}