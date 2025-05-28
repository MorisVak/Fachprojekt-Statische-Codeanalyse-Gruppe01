import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import org.opalj.BaseConfig

import scala.io.{Source, StdIn}
import scala.collection.mutable
import org.opalj.br.DeclaredMethod
import org.opalj.br.analyses.Project
import org.opalj.br.fpcf.properties.Context
import org.opalj.br.instructions.{CreateNewArrayInstruction, INVOKESTATIC, INVOKEVIRTUAL, MethodInvocationInstruction}
import org.opalj.log.GlobalLogContext
import org.opalj.tac.cg.RTACallGraphKey
import org.opalj.tac.cg.CHACallGraphKey
import org.opalj.tac.cg.XTACallGraphKey
import org.opalj.tac.cg.CFA_1_1_CallGraphKey

import java.net.URL

object thirdParty {
  def main(args: Array[String]): Unit = {

    val jarFile = new java.io.File("ErsteWochen.jar")
    var typeOfCallgraph: String = ""
    var usedMethods = Set[String]()

    implicit val config: Config =
      BaseConfig.withValue(
        "org.opalj.br.analyses.cg.InitialEntryPointsKey.analysis",
        ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.LibraryEntryPointsFinder")
      ).withValue(
        "org.opalj.br.analyses.cg.InitialInstantiatedTypesKey.analysis",
        ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.LibraryInstantiatedTypesFinder")
      )
    // --> Every public method is treated as entry point (sound)
    val project = Project(jarFile, GlobalLogContext, config)
    val libraryListFile = new java.io.File("libraries.txt")
    val libraryNames: Set[String] = Source.fromFile(libraryListFile)
      .getLines()
      .map(_.trim)
      .filter(_.nonEmpty)
      .toSet
    libraryNames.foreach { library =>
      println(library)
    }
    /*
    while (!(typeOfCallgraph.toLowerCase.contains("cha") ||
      typeOfCallgraph.toLowerCase.contains("rta") ||
      typeOfCallgraph.toLowerCase.contains("xta") ||
      typeOfCallgraph.toLowerCase.contains("1-1-cfa"))) {
        print("\nPlease enter type of callgraph (CHA, RTA, XTA, 1-1-CFA): ")
        typeOfCallgraph = StdIn.readLine()
    }
    println(s"\nType of callgraph: ${typeOfCallgraph.toUpperCase}")
    val cg = typeOfCallgraph match{
      case "cha" => project.get(CHACallGraphKey)
      case "rta" => project.get(RTACallGraphKey)
      case "xta" => project.get(XTACallGraphKey)
      case "1-1-cfa" => project.get(CFA_1_1_CallGraphKey)
    }
     */
    val cg = project.get(CHACallGraphKey)
    val classFiles = project.allClassFiles
    println(s"Number of classfiles: ${project.classFilesCount}")
    val countLibraryMethods = project.libraryMethodsCount

    cg.reachableMethods.foreach(method => {
      cg.calleesOf(method.method).foreach(callees => {
        callees._2.foreach(u => {
          libraryNames.foreach(library => {
            val decClassType = u.method.declaringClassType
            val isLibType = project.isLibraryType(decClassType)
            if (!usedMethods.contains(decClassType.toString()) && isLibType){
              usedMethods += u.method.declaringClassType.toString()
            }
          })
        })
      })
    })
    println(s"Count TPL Methods: ${countLibraryMethods}\nUsed TPL Methods: ${usedMethods.size}\n")
    /*
    classFiles.foreach { classFile =>
      val methods = classFile.methods
      methods.foreach { method =>
        method.body.foreach { line =>
          line.instructions.foreach { instruction =>
            if (instruction != null) {
              instruction match {
                case mi: MethodInvocationInstruction =>
                  if (project.isLibraryType(classFile)) {
                    println(s"Methode $method in der Klasse: ${method.classFile.fqn}")
                  }
                case _ =>
              }
            }
          }
        }
      }
    }
  }

 */

  }
}