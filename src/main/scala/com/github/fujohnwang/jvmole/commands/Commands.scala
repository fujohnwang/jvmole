package com.github.fujohnwang.jvmole.commands

import sbt.{AttributeKey, Command}
import sbt.complete.DefaultParsers._
import scala.Some
import com.github.fujohnwang.jvmole.{ProjectInfo}
import com.sun.tools.attach.VirtualMachine
import management.ManagementFactory
import collection.JavaConversions._
import com.github.fujohnwang.jvmole.jmx.LocalJMXConnector

trait Commands extends ProjectInfo with LocalJMXConnector {
  val VIRTUAL_MACHINE_K = AttributeKey[VirtualMachine]("virtual machine")

  def welcome = Command.command("welcome") {
    state => state.log.info("welcome to jvmole's world~"); state
  }

  /**
   * current JVMole process's pid will not be listed.
   */
  def listVirtualMachines = Command.command("lsvm", "list running virtual machine instances that we can attach to", "")(s => {
    s.log.info("JVM processes where are running: ")
    val processName = ManagementFactory.getRuntimeMXBean.getName
    VirtualMachine.list().foreach(vm => if (!processName.startsWith(vm.id())) s.log.info("  " + vm.toString))
    s
  })

  def attach = Command("attach", ("attach <pid>", "attach to specific virtual machine instance as per pid"), "")(_ => token(Space) ~> token(Digit.+.map(_.mkString), "<pid>"))((s, pid) => {
    val ns = s.put(VIRTUAL_MACHINE_K, VirtualMachine.attach(pid))
    s.log.info("attach to JVM instance with pid=" + pid + " successfully.")
    ns.addExitHook(() => detach)
  })

  def detach = Command.command("detach", "detach from attached JVM instance if any", "")(s => {
    s.get(VIRTUAL_MACHINE_K) match {
      case None => s.log.warn("no attached JVM instance found when trying to detach it.")
      case Some(vm) => s.log.info("detach JVM instance."); vm.detach()
    }
    s.remove(VIRTUAL_MACHINE_K)
  })

  def listMBeans = Command.command("beans", "list mbeans registered", "")(s => s)

  val setAttr = Command.command("set", "set attribute of some mbean", "")(s => s)

  val beanDesc = Command.command("desc", "describe mbean", "")(s => s)

  def execMBeanMethod = Command.command("exec", "invoke mbean method", "")(s => s)


}