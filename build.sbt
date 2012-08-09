organization := "com.github.fujohnwang"

name := "jvmole"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.9.2"

scalacOptions := Seq("-deprecation", "-unchecked", "-optimise")

javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

crossPaths := false

libraryDependencies ++= Seq(
			"org.scala-sbt" % "command" % "0.12.0",
			"org.ow2.asm" % "asm" % "4.0",
			"org.ow2.asm" % "asm-util" % "4.0",
			"com.google.guava" % "guava" % "13.0"
			)

resolvers <+= sbtResolver

initialCommands := """import sbt.complete._
import DefaultParsers._
"""

