package com.github.fujohnwang.jvmole.commands

import sbt.Command
import sbt.complete.DefaultParsers._
import javax.management.remote.{JMXServiceURL, JMXConnectorFactory, JMXConnector}
import com.sun.tools.attach.VirtualMachine
import java.io.File
import javax.management.ObjectName
import collection.JavaConversions._
import org.slf4j.helpers.MessageFormatter

trait JMXCommands extends VirtualMachineCommands {
  val CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress"

  def listMBeans = Command("beans", ("beans [pattern]", "list mbeans registered"), "")(s => (token(Space) ~> token(NotSpace, "<pattern>")).?)((s, p) => {
    for (vm <- s.get(VIRTUAL_MACHINE_K)) {
      execute(vm)(connector => {
        connector.getMBeanServerConnection.queryNames(p.map(n => new ObjectName(n)).getOrElse(null), null).foreach(objectName => s.log.info("  " + objectName.getCanonicalName))
      })
    }
    s
  })

  def beanDesc = Command("desc", ("desc <mbean>", "describe mbean"), "")(s => token(Space) ~> token(any.+ map (_.mkString), "<mbean>") <~ EOF)((s, mbean) => {
    for (vm <- s.get(VIRTUAL_MACHINE_K)) {
      execute(vm)(connector => {
        val conn = connector.getMBeanServerConnection
        val objectName = new ObjectName(mbean)
        conn.isRegistered(objectName) match {
          case false => s.log.warn("no such mbean found with object name='" + mbean + "'")
          case true => {
            val beanInfo = conn.getMBeanInfo(objectName)

            s.log.info(f("className: {}", beanInfo.getClassName))

            s.log.info("attributes: ")
            beanInfo.getAttributes.foreach(attr => {
              s.log.info(f("  name={}, type={}, readable={}, writable={}, isis={}", Array(attr.getName, attr.getType, attr.isReadable.toString, attr.isWritable.toString, attr.isIs.toString): _*))
            })

            s.log.info("operations: ")
            beanInfo.getOperations.foreach(op => {
              s.log.info(f("  {} {}({})", Array(op.getReturnType, op.getName, op.getSignature.map(mpi => Array(mpi.getType, mpi.getName).mkString(" ")).mkString(",")): _*))
            })
          }
        }
      })
    }
    s
  })

  def setAttr = Command.command("set", "set attribute of some mbean", "")(s => s)

  def execMBeanMethod = Command.command("exec", "invoke mbean method", "")(s => s)

  def execute[T](vm: VirtualMachine)(func: JMXConnector => T): T = {
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

  protected def f(format: String, arg: String): String = MessageFormatter.format(format, arg).getMessage

  protected def f(format: String, args: String*): String = MessageFormatter.arrayFormat(format, args.toArray[AnyRef]).getMessage
}