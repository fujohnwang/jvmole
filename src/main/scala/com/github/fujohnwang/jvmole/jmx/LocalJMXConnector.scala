package com.github.fujohnwang.jvmole.jmx

import java.io.File
import com.sun.tools.attach.VirtualMachine
import javax.management.remote.{JMXConnectorFactory, JMXServiceURL, JMXConnector}
import com.github.fujohnwang.jvmole.ProjectInfo

trait LocalJMXConnector extends ProjectInfo {
  val CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress"

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