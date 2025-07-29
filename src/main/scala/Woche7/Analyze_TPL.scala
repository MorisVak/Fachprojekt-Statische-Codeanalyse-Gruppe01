package Woche7

import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import org.opalj.BaseConfig
import java.io.PrintWriter
import scala.io.{Source, StdIn}
import scala.collection.mutable
import org.opalj.br.{ClassFile, DeclaredMethod, Type}
import org.opalj.br.analyses.Project
import org.opalj.br.fpcf.properties.Context
import org.opalj.br.instructions.{CreateNewArrayInstruction, INVOKESTATIC, INVOKEVIRTUAL, MethodInvocationInstruction}
import org.opalj.log.GlobalLogContext
import org.opalj.tac.cg.RTACallGraphKey
import org.opalj.tac.cg.CHACallGraphKey
import org.opalj.tac.cg.XTACallGraphKey
import org.opalj.tac.cg.CFA_1_1_CallGraphKey

import java.io.File
import java.net.URL


object Analyze_TPL {
  def main(jarFile: String, libraries: String, typeOfCallgraph: String): Unit = {
    var outputString = ""
    println("-----------TPL ANALYZER-----------\n")
    outputString += "-----------TPL ANALYZER-----------\n \n"
    // Pfade zu Projekt- und Bibliotheks-JARs
    val projectJar = new File(jarFile)
    val libraryJarNames = Source.fromFile(libraries).getLines().map(_.trim).filter(_.nonEmpty).toSet
    val libraryJars = libraryJarNames.map(name => new File(s"$name.jar"))

    implicit val config: Config =
      BaseConfig.withValue(
        "org.opalj.br.analyses.cg.InitialEntryPointsKey.analysis",
        ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.LibraryEntryPointsFinder")
      ).withValue(
        "org.opalj.br.analyses.cg.InitialInstantiatedTypesKey.analysis",
        ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.LibraryInstantiatedTypesFinder")
      )
    // --> Every public method is treated as entry point (sound)

    // 3. Alle JARs in eine java.util.List konvertieren
    val project = Project(projectJar, GlobalLogContext, config)



    //val callGraph = project.get(CHACallGraphKey)
    println(s"Number of classfiles: ${project.classFilesCount}")
    outputString += s"Number of classfiles: ${project.classFilesCount} \n"
    val totalMethodsPerLibrary = scala.collection.mutable.Map[String, Int]()
    val usedMethodsPerLibrary = scala.collection.mutable.Map[String, Int]().withDefaultValue(0)

    // Zähle alle Methoden pro Bibliothek
    libraryJars.foreach { jar =>
      val libProject = Project(jar)
      var methodCount = 0
      methodCount = libProject.methodsCount

      val libNameNoExt = jar.getName.split("-")(0)
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
      list += jar.split("-")(0).replace(".jar","")

    }
    val allSetsOfMethods: Map[String, mutable.Set[String]] = list.map(k => k -> mutable.Set.empty[String]).toMap
    val start = System.nanoTime()
    val callGraph = typeOfCallgraph match {
      case "cha" => project.get(CHACallGraphKey)
      case "rta" => project.get(RTACallGraphKey)
      case "xta" => project.get(XTACallGraphKey)
      case "1-1-cfa" => project.get(CFA_1_1_CallGraphKey)
    }
    // Durchlaufe den Callgraphen, um verwendete Methoden zu identifizieren
    callGraph.reachableMethods.foreach { rm =>
      callGraph.calleesOf(rm.method).foreach { case (_, callees) =>
        callees.foreach { callee =>
          val calleeType: Type = callee.method.declaringClassType
          // Schaue für jede Library, ob calleeType IN DER MENGE aller Klassen dieser Library ist:
          libraryClasses.foreach { case (libName, classSet) =>
            if (classSet.contains(calleeType)) {
              list.foreach { item =>
                if (calleeType.toString.contains(s"org/${item}")){
                  allSetsOfMethods(item) += callee.method.toString
                }
              }
            }
          }
        }
      }
    }
    val end = System.nanoTime()
    val duration = (end - start) / 1000000
    println(s"Laufzeit des Callgraphs: $duration Millisekunden")
    outputString += s"=== Ergebnisse der TPL-Analyse === \n \n Laufzeit des Callgraphs: $duration Millisekunden \n \n"
    // Ergebnisse ausgeben
    println("=== Ergebnisse der TPL-Analyse ===")
    list.toSeq.sorted.foreach { libName =>
      val total = totalMethodsPerLibrary.getOrElse(libName, 0)
      val used = allSetsOfMethods(libName).size
      val percent =
        if (total > 0) BigDecimal(used.toDouble / total * 100)
          .setScale(2, BigDecimal.RoundingMode.HALF_UP)
        else BigDecimal(0)

      println(s"Library: $libName.jar")
      println(s"  Gesamtmethoden:       $total")
      println(s"  Davon aufgerufen:     $used")
      println(f"  Nutzungsanteil:       $percent%2.2f%%")
      println()
      outputString += s"Library: $libName.jar \n   Gesamtmethoden:       $total \n   Davon aufgerufen:     $used \n " +
        f"  Nutzungsanteil:       $percent%2.2f%% \n \n"
    }

    val pw = new PrintWriter(" Results/TPL_result.txt")
    pw.write(outputString)
    pw.close()

    println("----------------------------------\n")
  }
}