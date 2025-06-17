package Woche4

import com.sun.tools.javap.resources.javap
import com.typesafe.config.{Config, ConfigValueFactory}
import org.opalj.BaseConfig
import org.opalj.ai.domain.l2.DefaultPerformInvocationsDomainWithCFGAndDefUse
import org.opalj.ai.fpcf.properties.AIDomainFactoryKey
import org.opalj.br.analyses.Project
import org.opalj.br.instructions.MethodInvocationInstruction
import org.opalj.log.GlobalLogContext
import org.opalj.tac.cg.RTACallGraphKey
import org.opalj.br.analyses._
import org.opalj.br.instructions._
import org.opalj.br._
import org.opalj.br.reader.Java8Framework
import org.opalj.io.write
import org.opalj.br.ClassFile
import org.opalj.br.reader.Java8Framework

import java.nio.file.Files
import java.nio.file.Paths
import scala.collection.mutable
import scala.io.Source
import java.util.jar.JarFile
import java.io.{File, FileOutputStream}
import java.nio.file.{Files, Paths}

object byteCodeModifier {
  def main(args: Array[String]):Unit = {
    var criticalCands = Array.empty[String]
    var suppressedMethods = Array.empty[String]
    var file = "Array.empty[String]"
    //Get input of the config.txt file
    val source = Source.fromFile("src/main/scala/Woche4/config.txt")
    try
      criticalCands = source.getLines().toArray
    finally
      source.close()

    //Get input of the supressedMethods.txt
    val source2 = Source.fromFile("src/main/scala/Woche4/supressedMethods.txt")
    try
      suppressedMethods = source2.getLines().toArray
    finally
      source2.close()

    //Get input of the file.txt
    val source3 = Source.fromFile("src/main/scala/Woche4/file.txt")
    try
      file = source3.getLines().mkString
    finally
      source3.close()

    println(file)

    var containsCriticalMethod = false
    var criticalMethodUsed = false
    //Sets to safe results
    val setOfContainedMethods = mutable.Set.empty[(String, String, String)]
    val setOfUsedMethods = mutable.Set.empty[(String, String, String)]

    implicit val config: Config =
      BaseConfig.withValue(
        "org.opalj.br.analyses.cg.InitialEntryPointsKey.analysis",
        ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.LibraryEntryPointsFinder")
      ).withValue(
        "org.opalj.br.analyses.cg.InitialInstantiatedTypesKey.analysis",
        ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.LibraryInstantiatedTypesFinder")
      )
    // --> Every public method is treated as entry point (sound)

    val performInvocationsDomain = classOf[DefaultPerformInvocationsDomainWithCFGAndDefUse[_]]

    val jarFile = new java.io.File(file)
    val project = Project(jarFile, GlobalLogContext, config)

    project.updateProjectInformationKeyInitializationData(AIDomainFactoryKey) {
      case None ⇒ Set(performInvocationsDomain)
      case Some(requirements) ⇒ requirements + performInvocationsDomain
    }
    //Look through all classes
    val classFiles = project.allClassFiles
    classFiles.foreach { classFile =>
      val methods = classFile.methods
      methods.filter(method => !suppressedMethods.contains(method.name)).foreach { method =>
        val body = method.body
        body.foreach {
          line =>
            line.instructions.foreach {
              case invokedMethod: MethodInvocationInstruction =>
                //check if the critical methods are contained in the project.
                criticalCands.foreach(cand => if (cand == invokedMethod.name) {
                  containsCriticalMethod = true
                  setOfContainedMethods += ((cand, method.fullyQualifiedSignature, classFile.fqn))
                })
              case _ =>
            }
        }
      }
    }
    //if critical method is contained, create an RTA CallGraph
    if (containsCriticalMethod) {
      println("FOUND CRITICAL METHOD : ")
      setOfContainedMethods.foreach(contained => println(s"- ${contained._1} in class ${contained._2} "))
      println("Validating if critical methods are being used : ")

      //Create RTA Callgraph
      val cg = project.get(RTACallGraphKey)

      cg.reachableMethods.foreach(c => {
        cg.calleesOf(c.method).foreach(p => {
          p._2.foreach(u => {
            setOfContainedMethods.foreach(potentialMethod => {
              if (u.method.name == potentialMethod._1) {
                setOfUsedMethods += ((potentialMethod._1, potentialMethod._2, potentialMethod._3))
                criticalMethodUsed = true
              }
            })
          })
        })
      })
      if (criticalMethodUsed) {
        println("WARNING CRITICAL METHODS ARE BEING USED : ")
        setOfUsedMethods.foreach(contained => println(s"- ${contained._1} in method ${contained._2}" +
          s" in class ${contained._3} "))
      } else {
        println("CRITICAL METHODS EXIST BUT ARE NOT BEING USED")
        println("please check : ")
        setOfContainedMethods.foreach(method =>
          println(s"- ${method._1} in method ${method._2} in class ${method._3} "))
      }
    } else {
      println("\n No critical methods found :)")
    }

    val usedClasses = setOfUsedMethods.map(_._3).toSet
    val tempDir = unpackJarToTempDir(jarFile.getAbsolutePath)
    usedClasses.foreach { classFile =>
          printBytecodeFromJavap(classFile,tempDir.getAbsolutePath)
    }

  }



  def unpackJarToTempDir(jarPath: String): File = {
    val jarFile = new JarFile(jarPath)
    val tempDir = Files.createTempDirectory("jar-unpack").toFile

    val entries = jarFile.entries()
    while (entries.hasMoreElements) {
      val entry = entries.nextElement()
      if (!entry.isDirectory && entry.getName.endsWith(".class")) {
        val outFile = new File(tempDir, entry.getName)
        outFile.getParentFile.mkdirs()
        val in = jarFile.getInputStream(entry)
        val out = new FileOutputStream(outFile)
        in.transferTo(out)
        in.close()
        out.close()
      }
    }

    tempDir
  }

  def printBytecodeFromJavap(className: String, classPath: String): Unit = {
    val pb = new ProcessBuilder("javap", "-c", "-p", "-classpath", classPath, className)
    pb.redirectErrorStream(true)
    val process = pb.start()

    val output = scala.io.Source.fromInputStream(process.getInputStream).getLines()
    output.foreach(println)
    process.waitFor()
  }

}

