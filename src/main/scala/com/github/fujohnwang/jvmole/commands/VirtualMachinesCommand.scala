package com.github.fujohnwang.jvmole.commands

import sbt.Command
import com.sun.tools.attach.VirtualMachine
import collection.JavaConversions._
import management.ManagementFactory

trait VirtualMachinesCommand {
  /**
   * current JVMole process's pid will not be listed.
   */
  def listVirtualMachines = Command.command("vms", "list running virtual machine instances that we can attach to", "")(s => {
    s.log.info("JVM processes where are running: ")
    val processName = ManagementFactory.getRuntimeMXBean.getName
    VirtualMachine.list().foreach(vm => if (!processName.startsWith(vm.id())) s.log.info("  " + vm.toString))
    s
  })
}