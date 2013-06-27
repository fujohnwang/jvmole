organization := "me.afoo"

name := "publisher"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.2"

scalacOptions := Seq("-deprecation", "-unchecked", "-optimise")

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

crossPaths := false

libraryDependencies ++= Seq(
            "ch.qos.logback" % "logback-core" % "1.0.13",
            "ch.qos.logback" % "logback-classic" % "1.0.13",
			"org.scala-sbt" % "command" % "0.12.3",
			"com.typesafe" % "config" % "1.0.1",
			"org.eclipse.jgit" % "org.eclipse.jgit" % "2.3.1.201302201838-r",
			"net.schmizz" % "sshj" % "0.8.1"
			)

resolvers <+= sbtResolver

initialCommands := """import sbt.complete._
import DefaultParsers._
"""

