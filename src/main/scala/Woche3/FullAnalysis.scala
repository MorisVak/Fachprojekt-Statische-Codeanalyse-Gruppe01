
/*
import com.typesafe.config.{Config, ConfigValueFactory}

import org.opalj.BaseConfig
import org.opalj.br.{ClassFile, Type}
import org.opalj.br.analyses.Project
import org.opalj.log.GlobalLogContext
import org.opalj.tac.cg.{CFA_1_1_CallGraphKey, CHACallGraphKey, RTACallGraphKey, XTACallGraphKey}

import java.io.File
import scala.collection.mutable
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
    val CFAcallGraph = project.get(CHACallGraphKey)

    val totalMethodsPerLibrary = Map[String, mutable.Set[String]]
    val usedMethodsPerLibraryCHA = Map[String, mutable.Set[String]]
    val usedMethodsPerLibraryRTA = Map[String, mutable.Set[String]]
    val usedMethodsPerLibraryXTA = Map[String, mutable.Set[String]]
    val usedMethodsPerLibraryCFA = Map[String, mutable.Set[String]]

    // Zähle alle Methoden pro Bibliothek
    libraryJars.foreach { jar =>
      val libProject = Project(jar)
      var methodCount = 0
      methodCount = libProject.methodsCount

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

    var list = scala.collection.mutable.ListBuffer[String]()
    libraryJarNames.foreach { jar =>
      list += jar.split("-")(0).replace(".jar", "")
    }
    val allSetsOfMethods: Map[String, mutable.Set[String]] = list.map(k => k -> mutable.Set.empty[String]).toMap
    val nums = Seq(1, 2, 3) //Anzahl der Durchlaeufe
    val durationCHA: Long = 0
    var durationRTA: Long = 0
    var durationXTA: Long = 0
    var durationCFA: Long = 0
    val durations = ListBuffer(durationCHA, durationRTA, durationXTA, durationCFA)
    val graphs = Seq(CHAcallGraph, RTAcallGraph, XTAcallGraph, CFAcallGraph)
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
                if (classSet.contains(calleeType) && j==1) {
                  list.foreach { item =>
                    if (calleeType.toString.contains(s"org/${item}")) {
                      allSetsOfMethods(item) += callee.method.toString
                      iteratorDurations match {
                        case 0 => usedMethodsPerLibraryCHA(item) += callee.method.toString
                        case 1 => usedMethodsPerLibraryRTA(item) += callee.method.toString
                        case 2 => usedMethodsPerLibraryXTA(item) += callee.method.toString
                        case 3 => usedMethodsPerLibraryCFA(item) += callee.method.toString
                      }

                    }
                  }
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
    var graphIterator = 0
    println("ERGEBNISSE: \n")
    graphs.foreach{ cg =>
      libraryJarNames.toSeq.sorted.foreach { libName =>
        val total = totalMethodsPerLibrary.getOrElse(libName, 0)
        var used: Int = 0
        graphIterator match {
          case 0 => {
            used = usedMethodsPerLibraryCHA.size
            println(s"Callgraph: CHACallGraph")
            println(s" Laufzeit:             ${durations(iteratorDurations)}")
          }
          case 1 => {
            used = usedMethodsPerLibraryRTA.size
            println(s"Callgraph: RTACallGraph")
            println(s" Laufzeit:             ${durations(iteratorDurations)}")
          }
          case 2 => {
            used = usedMethodsPerLibraryXTA.size
            println(s"Callgraph: XTACallGraph")
            println(s" Laufzeit:             ${durations(iteratorDurations)}")
          }
          case 3 => {
            used = usedMethodsPerLibraryCFA.size
            println(s"Callgraph: CFACallGraph")
            println(s" Laufzeit:             ${durations(iteratorDurations)}")
          }
        }
        val percent =
          if (total > 0) BigDecimal(used.toDouble / total * 100)
            .setScale(2, BigDecimal.RoundingMode.HALF_UP)
          else BigDecimal(0)

        println(s"Library: $libName.jar")
        println(s"  Gesamtmethoden:       $total")
        println(s"  Davon aufgerufen:     $used")
        println(f"  Nutzungsanteil:       $percent%2.2f%%")

        println()

      }
      graphIterator += 1
      iteratorDurations += 1
    }
  }
}

 */