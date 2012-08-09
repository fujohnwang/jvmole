package com.github.fujohnwang.jvmole

import xsbti.AppConfiguration

/**
 * for easy testing by simple 'sbt run'
 */
object JVMoleMain {
  def main(args: Array[String]) {
    new JVMole().run(new AppConfiguration() {
      def arguments() = args

      def baseDirectory() = null

      def provider() = null
    })
  }
}