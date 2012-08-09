package com.github.fujohnwang.jvmole

import com.sun.tools.attach.VirtualMachine

/**
 * I am not sure whether resource registry way is a good way in such a command line app with sbt to manage states.
 * But let's just make it run first.
 */
object JVMoleHeadQuarter extends ProjectInfo {
  var virtualMachine: Option[VirtualMachine] = None

}