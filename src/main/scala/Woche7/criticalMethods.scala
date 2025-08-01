package Woche7

import com.typesafe.config.{Config, ConfigValueFactory}
import java.io.PrintWriter
import org.opalj.BaseConfig
import org.opalj.ai.domain.l2.DefaultPerformInvocationsDomainWithCFGAndDefUse
import org.opalj.ai.fpcf.properties.AIDomainFactoryKey
import org.opalj.br.analyses.Project
import org.opalj.br.instructions.MethodInvocationInstruction
import org.opalj.log.GlobalLogContext
import org.opalj.tac.cg.RTACallGraphKey

import scala.collection.mutable
import scala.io.Source

object criticalMethods{
  def main(conf: String, supprMethods: String, file: String):Unit = {
    var outputString = ""
    println("-----------CRITICAL METHODS ANALYZER-----------\n")
    outputString += s"-----------CRITICAL METHODS ANALYZER----------- \n \n"
    var criticalCands = Array.empty[String]
    var suppressedMethods = Array.empty[String]
    //Get input of the config.txt file
    val source = Source.fromFile(conf)
    try
      criticalCands = source.getLines().toArray
    finally
      source.close()

    //Get input of the supressedMethods.txt
    val source2 = Source.fromFile(supprMethods)
    try
      suppressedMethods = source2.getLines().toArray
    finally
      source2.close()


    var containsCriticalMethod = false
    var criticalMethodUsed = false
    //Sets to safe results
    val setOfContainedMethods = mutable.Set.empty[(String,String, String)]
    val setOfUsedMethods = mutable.Set.empty[(String,String,String)]

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
      case None               ⇒ Set(performInvocationsDomain)
      case Some(requirements) ⇒ requirements + performInvocationsDomain
    }
    //Look through all classes
    val classFiles = project.allClassFiles
    classFiles.foreach{ classFile =>
      val methods  = classFile.methods
      methods.filter(method => !suppressedMethods.contains(method.name)).foreach{method =>
        val body = method.body
        body.foreach{
          line => line.instructions.foreach{
            case invokedMethod: MethodInvocationInstruction =>
              //check if the critical methods are contained in the project.
              criticalCands.foreach(cand => if(cand == invokedMethod.name){
                containsCriticalMethod = true
                setOfContainedMethods += ((cand, method.fullyQualifiedSignature ,classFile.fqn))
              })
            case _ =>
          }
        }
      }
    }
    //if critical method is contained, create an RTA CallGraph
    if(containsCriticalMethod){
      println("FOUND CRITICAL METHOD : ")
      outputString += s"FOUND CRITICAL METHOD : \n \n"
      setOfContainedMethods.foreach(contained => {
        println(s"- ${contained._1} in class ${contained._2} ")
        outputString += s"- ${contained._1} in class ${contained._2} \n"
      })
      println("Validating if critical methods are being used : ")
      outputString += "Validating if critical methods are being used : \n \n"
      //Create RTA Callgraph
      val cg = project.get(RTACallGraphKey)

      cg.reachableMethods.foreach(c => {
        cg.calleesOf(c.method).foreach(p => {
          p._2.foreach(u => {
            setOfContainedMethods.foreach(potentialMethod => {
              if(u.method.name == potentialMethod._1 ){
                setOfUsedMethods += ((potentialMethod._1,potentialMethod._2, potentialMethod._3))
                criticalMethodUsed = true
              }
            })
          })
        })
      })
      if(criticalMethodUsed){
        println("WARNING CRITICAL METHODS ARE BEING USED : ")
        outputString += s"WARNING CRITICAL METHODS ARE BEING USED : \n"
        setOfUsedMethods.foreach(contained => {
          println(s"- ${contained._1} in method ${contained._2}" +
            s" in class ${contained._3} ")
          outputString += s"- ${contained._1} in method ${contained._2}" +
            s" in class ${contained._3}  \n"
        })
      }else{
        println("CRITICAL METHODS EXIST BUT ARE NOT BEING USED")
        println("please check : ")
        outputString += "CRITICAL METHODS EXIST BUT ARE NOT BEING USED \n please check : \n \n"
        setOfContainedMethods.foreach(method => {
          println(s"- ${method._1} in method ${method._2} in class ${method._3} ")
          outputString += s"- ${method._1} in method ${method._2} in class ${method._3} \n"
        })
      }
    }else{
      println("\n No critical methods found :)")
      outputString += " No critical methods found :) \n"
    }

    val pw = new PrintWriter(" Results/criticalMethods_result.txt")
    pw.write(outputString)
    pw.close()
    println("-----------------------------------------------\n")
  }

  /*
  * Case wo man es benutzen könnte --> explizite Testung des Systems
  * Umsetzung --> constraints.txt in der die Cases stehen, wo die Prüfung ausgesetzt werden soll.
  * */
}