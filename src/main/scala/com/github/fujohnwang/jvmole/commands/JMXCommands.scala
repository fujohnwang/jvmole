package com.github.fujohnwang.jvmole.commands

import sbt.Command
import javax.management.remote.{JMXServiceURL, JMXConnectorFactory, JMXConnector}
import com.sun.tools.attach.VirtualMachine
import java.io.File

trait JMXCommands extends VirtualMachineCommands {
  val CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress"

  def listMBeans = Command.command("beans", "list mbeans registered", "")(s => s)

  def beanDesc = Command.command("desc", "describe mbean", "")(s => s)

  def setAttr = Command.command("set", "set attribute of some mbean", "")(s => s)

  def execMBeanMethod = Command.command("exec", "invoke mbean method", "")(s => s)

  def execute[T](func: JMXConnector => T)(implicit vm: VirtualMachine): T = {
    val connector = JMXConnectorFactory.connect(new JMXServiceURL(getLocalJMXConnectorAddress(vm)))
    try {
      func(connector)
    } finally {
      connector.close()
    }
  }

  protected def getLocalJMXConnectorAddress(vm: VirtualMachine): String = {
    vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS) match {
      case null => {
        val agent = vm.getSystemProperties().getProperty("java.home") + File.separator + "lib" + File.separator + "management-agent.jar"
        vm.loadAgent(agent)
        vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS)
      }
      case address@_ => address
    }
  }
}