package com.github.fujohnwang.jvmole.commands

import sbt.Command
import sbt.complete.DefaultParsers._
import javax.management.remote.{JMXServiceURL, JMXConnectorFactory, JMXConnector}
import com.sun.tools.attach.VirtualMachine
import java.io.File
import javax.management.{Attribute, ObjectName}
import collection.JavaConversions._
import org.slf4j.helpers.MessageFormatter
import scala.Some

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

  /**
   * (space, Quote ~ name~ Quote | name  , space, attributeName, space, value, space*)
   * @return
   */
  def setAttr = Command("set", ("set <mbean> <attribute name> <attribute value>", "set attribute of some mbean"), "")(_ => attrSetParser)((s, args) => {
    for (vm <- s.get(VIRTUAL_MACHINE_K)) {
      execute(vm)(connector => {
        val objectName = new ObjectName(args(0))
        val attributeName = args(1)
        val attributeValue = args(2)
        val conn = connector.getMBeanServerConnection
        conn.isRegistered(objectName) match {
          case false => s.log.warn("no mbean found with object name='" + args(0) + "'")
          case true => {
            conn.getMBeanInfo(objectName).getAttributes.find(_.getName.equals(attributeName)) match {
              case Some(attrInfo) => {
                // TODO type conversion
                conn.setAttribute(objectName, new Attribute(attributeName, attributeValue))
              }
              case None => s.log.warn("no attribute found with name=" + attributeName)
            }
          }
        }
      })
    }
    s
  })

  def mbeanParser = (token(Space) ~> token((DQuoteClass ~> (NotDQuoteClass).+ <~ DQuoteClass).string.||(NotSpace) map (r => r.left.getOrElse(r.left.get)), "<object name>"))

  def attrParser = (token(Space) ~> token(NotSpace, "<attributeName>"))

  def attrSetParser = mbeanParser ~ attrParser ~ (token(Space) ~> token(any.+.string, "<attributeValue>") <~ SpaceClass.*) map (r => Seq(r._1._1, r._1._2, r._2))

  lazy val NotDQuoteClass =
    charClass({
      c: Char => (c != DQuoteChar)
    }, "non-double-quote-space character")


  def methodNameParser = (token(Space) ~> token(NotSpace, "<methodName>"))

  def execParser = mbeanParser ~ methodNameParser ~ (token(Space) ~> token(StringBasic, "<args>")).* <~ SpaceClass.*

  /**
   * limitation: only allows to invoke mbean methods with primitive type arguments
   * <pre>
   * <code> exec mbean method args* </code>
   * </pre>
   * @return
   */
  def execMBeanMethod = Command("exec", ("exec <mbean> <method name> <method args>*", "invoke mbean method"), "")(_ => execParser)((s, format) => {
    val (mbeanName, methodName) = format._1
    val arguments = format._2
    for (vm <- s.get(VIRTUAL_MACHINE_K)) {
      execute(vm)(connector => {
        val conn = connector.getMBeanServerConnection
        val objectName = new ObjectName(mbeanName)
        conn.isRegistered(objectName) match {
          case true => {
            conn.getMBeanInfo(objectName).getOperations.find(mi => mi.getName == methodName && mi.getSignature.length == arguments.size) match {
              case None => s.log.error(f("no method with name={} on mbean:{} found to execute.", methodName, mbeanName))
              case Some(operation) => {
                val result = conn.invoke(objectName, methodName, arguments.toArray[AnyRef], operation.getSignature.map(_.getType))
                s.log.info(f("invoke method:{} on mbean:{} successfully with result={}", methodName, mbeanName, result.toString))
              }
            }
          }
          case false => s.log.error("no such mbean registered: " + mbeanName)
        }
      })
    }
    s
  })

  protected def execute[T](vm: VirtualMachine)(func: JMXConnector => T): T = {
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