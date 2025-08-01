name := "Fachprojekt"

version := "0.1"

scalaVersion := "2.13.12"
Compile / mainClass := Some("Woche7.AktionenAuswahl")


libraryDependencies += "de.opal-project" % "framework_2.13" % "5.0.0"
libraryDependencies += "com.lihaoyi" %% "upickle" % "4.1.0"
libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.11.3"
libraryDependencies += "io.circe" %% "circe-core"    % "0.14.13"
libraryDependencies += "io.circe" %% "circe-generic" % "0.14.13"
libraryDependencies += "io.circe" %% "circe-parser" % "0.14.13"
libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0"
libraryDependencies += "com.lihaoyi" %% "requests" % "0.9.0"
libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "3.0.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test"
