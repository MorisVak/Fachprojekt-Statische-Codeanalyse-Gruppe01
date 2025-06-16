package Woche4
import com.typesafe.config.{Config, ConfigValueFactory}
import org.opalj.BaseConfig
import org.opalj.br.analyses.Project
import org.opalj.log.GlobalLogContext
import org.opalj.tac.cg._
import org.opalj.ba._
import org.opalj.br._
import org.opalj.br.instructions._
import org.opalj.ba._
import org.opalj.bc.Assembler
import org.opalj.br._
import org.opalj.br.instructions._
import org.opalj.util.InMemoryClassLoader

import java.io.IOException
import java.nio.file.{Files, Paths}
import java.nio.file.StandardOpenOption
import scala.collection.mutable
import scala.io.Source

object byteCodeCreator {
  def main(args: Array[String]):Unit = {
    implicit val config: Config =
      BaseConfig.withValue(
        "org.opalj.br.analyses.cg.InitialEntryPointsKey.analysis",
        ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.LibraryEntryPointsFinder")
      ).withValue(
        "org.opalj.br.analyses.cg.InitialInstantiatedTypesKey.analysis",
        ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.LibraryInstantiatedTypesFinder")
      )


    val project = Project(new java.io.File("openmrs-api-2.8.0-SNAPSHOT.jar"), GlobalLogContext, config)
    val firstProject = Project(new java.io.File("openmrs-api-2.8.0-SNAPSHOT.jar"))
    val TPLproject = Project(new java.io.File ("hibernate-core-5.6.15.Final.jar"))

    val setOfusedMethodsTPL = mutable.Set.empty[String]

    TPLproject.packages

    println(TPLproject.packages)

    val cg = project.get(RTACallGraphKey)
    cg.reachableMethods.foreach(c => {
      cg.calleesOf(c.method).foreach(p => {
        p._2.foreach(u => {
          //println("--------------------------")
          //println(s" PACKAGE :: ${u.method.declaringClassType.packageName}")
          TPLproject.packages.foreach(pack => {
            //println(pack)
            if (u.method.declaringClassType.packageName == pack){
              setOfusedMethodsTPL += u.method.descriptor.toString()
            }
          } )
        })
      })
    })

    setOfusedMethodsTPL.foreach(string => {
      println(string)
    })
    setOfusedMethodsTPL.toSet
    println(s"FINAL AMOUNT = ${setOfusedMethodsTPL.size}")

    var count = 0

    val hibernateClassFiles = TPLproject.allClassFiles
    var hasUsage = false
    hibernateClassFiles.foreach{ file =>
      var arr = Array[Method]()
      hasUsage = false
      file.methods.foreach( method => {
        setOfusedMethodsTPL.foreach( usedMethod => {
          //println(s" Method used :${file.fqn} ${method.name} --- ${usedMethod}")
          if( usedMethod == method.descriptor.toString()){
            arr = arr.appended(method)
            hasUsage = true
          }
        })
      })
      if(hasUsage) {
        createClass(file, arr)
        count += 1
      }

    }
    println(count)
    println("hib files:" + hibernateClassFiles.size)
//    val cb = CLASS(
//      accessModifiers = PUBLIC,
//      thisType = "Test",
//      methods = METHODS(
//        METHOD(PUBLIC, "<init>", "()V", CODE(
//          // The following instruction is annotated with some meta information
//          // which can later be used; e.g., to check that some static analysis
//          // produced an expected result when this instruction is reached.
//          RETURN -> null
//        ))
//      )
//    )
//
//    val (daClassFile, codeAnnotations) = cb.toDA()
//    val rawClassFile : Array[Byte] = org.opalj.bc.Assembler(daClassFile)
//
//    val outputFile = Paths.get("src/main/scala/Woche4/Test.class")
//    Files.createDirectories(outputFile.getParent)
//    Files.write(outputFile, rawClassFile)

  }

  def createClass(file: ClassFile, arr: Array[Method]): Unit = {
    val generatedMethods = arr.map { m =>
      // Für jede vorhandene Methode erzeugen wir eine Dummy-Methode im neuen Bytecode
      val descriptor = m.descriptor.toJVMDescriptor
      val name = m.name

      METHOD(PUBLIC, name, descriptor, CODE(
        RETURN -> null
      ))
    }

    val cb = CLASS(
      accessModifiers = PUBLIC,
      thisType = file.fqn,
      methods = METHODS(
        generatedMethods.toSeq: _* // alle Methoden einfügen
      )
    )
    val (daClassFile, _) = cb.toDA()
    val rawClassFile: Array[Byte] = Assembler(daClassFile)
    val sanName = sanitizeForFilename(file.thisType.simpleName)
    val outputPath = Paths.get("src/main/scala/Woche4/dummies", sanName + ".class")
    try {
      Files.createDirectories(outputPath.getParent)
      Files.write(outputPath, rawClassFile)
    } catch {
      case e: IOException =>
        println(s" Konnte Datei für ${file.fqn} nicht schreiben: ${e.getMessage}")
    }

  }

  def sanitizeForFilename(name: String): String = {
    // Erlaubt nur Buchstaben, Zahlen, Unterstriche und Punkte
    name.replaceAll("[^a-zA-Z0-9_.]", "_")
  }

}
