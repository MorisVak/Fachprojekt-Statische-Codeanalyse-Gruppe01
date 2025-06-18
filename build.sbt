name := "Fachprojekt"

version := "0.1"

scalaVersion := "2.13.12"

libraryDependencies += "de.opal-project" % "framework_2.13" % "5.0.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test"

ThisBuild / evictionErrorLevel := Level.Warn