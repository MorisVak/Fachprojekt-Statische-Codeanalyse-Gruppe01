import com.typesafe.config.{Config, ConfigValueFactory}
import org.opalj.BaseConfig
import org.opalj.br.{ClassFile, Type}
import org.opalj.br.analyses.Project
import org.opalj.log.GlobalLogContext
import org.opalj.tac.cg.{CFA_1_1_CallGraphKey, CHACallGraphKey, RTACallGraphKey, XTACallGraphKey}

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.io.Source

object FullAnalysis {
  def main(args: Array[String]): Unit = {
    // Pfade zu Projekt- und Bibliotheks-JARs
    val projectJar = new File("openmrs-api-2.8.0-SNAPSHOT.jar")
    val libraryJarNames = Source.fromFile("libraries.txt").getLines().map(_.trim).filter(_.nonEmpty).toSet
    val libraryJars = libraryJarNames.map(name => new File(s"$name.jar"))

    implicit val config: Config =
      BaseConfig.withValue(
        "org.opalj.br.analyses.cg.InitialEntryPointsKey.analysis",
        ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.LibraryEntryPointsFinder")
      ).withValue(
        "org.opalj.br.analyses.cg.InitialInstantiatedTypesKey.analysis",
        ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.LibraryInstantiatedTypesFinder")
      )
    val project = Project(projectJar, GlobalLogContext, config)
    val CHAcallGraph = project.get(CHACallGraphKey)
    val RTAcallGraph = project.get(CHACallGraphKey)
    val XTAcallGraph = project.get(CHACallGraphKey)
    //val CFAcallGraph = project.get(CHACallGraphKey)

    val totalMethodsPerLibrary = scala.collection.mutable.Map[String, Int]()
    val usedMethodsPerLibrary = scala.collection.mutable.Map[String, Int]().withDefaultValue(0)

    // Zähle alle Methoden pro Bibliothek
    libraryJars.foreach { jar =>
      val libProject = Project(jar)
      var methodCount = 0
      libProject.allClassFiles.foreach { libClassFile =>
        methodCount += libClassFile.methods.size
      }

      val libNameNoExt = jar.getName.stripSuffix(".jar")
      totalMethodsPerLibrary(libNameNoExt) = methodCount
    }

    val libraryClasses: Map[String, Set[Type]] = libraryJars.map { jarFile =>
      val libName = jarFile.getName.stripSuffix(".jar")
      val libProj = Project(jarFile)

      val typesInLib: Set[Type] = libProj.allProjectClassFiles.map { classFile: ClassFile =>
        classFile.thisType
      }.toSet

      libName -> typesInLib
    }.toMap

    val nums = Seq(1, 2, 3)
    val durationCHA: Long = 0
    var durationRTA: Long = 0
    var durationXTA: Long = 0
    //var durationCFA: Long = 0
    val durations = ListBuffer(durationCHA, durationRTA, durationXTA)
    val graphs = Seq(CHAcallGraph, RTAcallGraph, XTAcallGraph) //, CFAcallGraph)
    var start: Long = 0
    var end: Long = 0
    var duration: Long = 0
    var iteratorDurations: Int = 0
    for (callGraph <- graphs) {
      for (j <- nums) {
        start = System.nanoTime()
        callGraph.reachableMethods.foreach { rm =>
          callGraph.calleesOf(rm.method).foreach { case (_, callees) =>
            callees.foreach { callee =>
              val calleeType: Type = callee.method.declaringClassType

              // Schaue für jede Library, ob calleeType IN DER MENGE aller Klassen dieser Library ist:
              libraryClasses.foreach { case (libName, classSet) =>
                if (classSet.contains(calleeType)) {
                  // Fund: diese callee-Methode kommt aus genau dieser Library
                  usedMethodsPerLibrary(libName) += 1
                }
              }
            }
          }
        }
        end = System.nanoTime()
        duration += (end-start)
      }
      duration = duration / nums.size
      durations(iteratorDurations) = duration
      iteratorDurations += 1
    }
    iteratorDurations = 0
    println("ERGEBNISSE: \n")
    graphs.foreach { cg =>
      iteratorDurations match{
        case 0 => println("Callgraph: CHACallGraph")
        case 1 => println("Callgraph: RTACallGraph")
        case 2 => println("Callgraph: XTACallGraph")
        case 3 => println("Callgraph: CFA_1_1_CallGraph")
      }
      println(s"  AVG Duration: ${durations(iteratorDurations)/1000000} milliseconds")
      iteratorDurations+=1
    }
  }
}