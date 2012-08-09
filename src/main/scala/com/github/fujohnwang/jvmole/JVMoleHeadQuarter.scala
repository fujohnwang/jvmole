package com.github.fujohnwang.jvmole

import com.sun.tools.attach.VirtualMachine

object JVMoleHeadQuarter extends ProjectInfo {
  var virtualMachine: Option[VirtualMachine] = None

}