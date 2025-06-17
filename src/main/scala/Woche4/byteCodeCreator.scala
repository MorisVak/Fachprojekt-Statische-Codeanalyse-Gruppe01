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
import java.net.URL
import java.nio.file.{Files, Paths}
import java.nio.file.StandardOpenOption
import scala.collection.mutable
import scala.io.Source

object byteCodeCreator {

  def loadProject(path: String,config : Config): Project[URL] = {
    Project(new java.io.File(path), GlobalLogContext, config)
  }

  def loadTPLProject(path: String): Project[URL] = {
    Project(new java.io.File(path))
  }

  def main(args: Array[String]): Unit = {

    implicit val config: Config = defaultConfig
    val project = loadProject("openmrs-api-2.8.0-SNAPSHOT.jar", config)
    val TPLproject = loadTPLProject("hibernate-core-5.6.15.Final.jar")

    val usedMethods = analyzeCallGraph(project, TPLproject)

    println(s"Classes Amount = ${usedMethods.map(_._1.fqn).size}")
    println(s"FINAL AMOUNT = ${usedMethods.size}")

    generateDummyClasses(usedMethods)

    println(usedMethods.size)

    println("Done.")
  }

  def defaultConfig: Config = {
    BaseConfig.withValue(
      "org.opalj.br.analyses.cg.InitialEntryPointsKey.analysis",
      ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.LibraryEntryPointsFinder")
    ).withValue(
      "org.opalj.br.analyses.cg.InitialInstantiatedTypesKey.analysis",
      ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.LibraryInstantiatedTypesFinder")
    )
  }

  def analyzeCallGraph(project: Project[URL], TPLproject: Project[URL]):
  (Set[(ObjectType, String, MethodDescriptor)]) = {

    val setOfUsedMethods = mutable.Set.empty[(ObjectType, String, MethodDescriptor)]


    val cg = project.get(RTACallGraphKey)

    cg.reachableMethods.foreach { c =>
      cg.calleesOf(c.method).foreach { p =>
        p._2.foreach { u =>
          val declaringType = u.method.declaringClassType
          val methodTriple = (declaringType, u.method.name, u.method.descriptor)


          if (TPLproject.packages.contains(declaringType.packageName)) {
            setOfUsedMethods += methodTriple
          }
        }
      }
    }

    (setOfUsedMethods.toSet)
  }

  def generateDummyClasses(usedMethods: Set[(ObjectType, String, MethodDescriptor)]): Unit = {
    val grouped = usedMethods.groupBy(_._1)

    grouped.foreach { case (objType, methods) =>
      createClassFromTriples(objType, methods)
    }
  }

  def createClassFromTriples(objectType: ObjectType, methods: Iterable[(ObjectType, String, MethodDescriptor)]): Unit = {
    val generatedMethods = methods.collect {
      case (_, name, descriptor) =>
        if(name.contains("void")) {
          METHOD(PUBLIC, name, descriptor.toJVMDescriptor, CODE(RETURN))
        } else {
          METHOD(PUBLIC, name, descriptor.toJVMDescriptor, CODE(ACONST_NULL, ARETURN))
        }

    }.toSeq

    // Wenn KEIN Konstruktor dabei ist, darf keiner erzeugt werden.
    val cb = CLASS(
      accessModifiers = PUBLIC,
      thisType = objectType.fqn,
      methods = METHODS(generatedMethods: _*)
    )

    val (daClassFile, _) = cb.toDA()
    val rawClassFile: Array[Byte] = Assembler(daClassFile)
    val sanName = sanitizeForFilename(objectType.simpleName)
    val outputPath = Paths.get("src/main/scala/Woche4/dummies", sanName + ".class")

    try {
      Files.createDirectories(outputPath.getParent)
      Files.write(outputPath, rawClassFile)
    } catch {
      case e: IOException =>
        println(s"Konnte Datei für ${objectType.fqn} nicht schreiben: ${e.getMessage}")
    }
  }

  def sanitizeForFilename(name: String): String = {
    name.replaceAll("[^a-zA-Z0-9_.]", "_")
  }
}

