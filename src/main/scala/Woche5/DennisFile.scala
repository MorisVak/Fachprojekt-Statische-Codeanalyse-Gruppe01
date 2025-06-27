package Woche5

import org.opalj.ai.{AIResult, BaseAI}
import org.opalj.ai.common.{DomainRegistry, XHTML}
import org.opalj.br.analyses.Project

import java.io.File
import java.nio.file.{Files, Path}

object DennisFile {

  def main(args: Array[String]): Unit = {
    val domainDescriptions = DomainRegistry.domainDescriptions

    val input = 12

    var cnt = 0
    val domainMap = domainDescriptions.map{ descr =>
      val t = (cnt, descr)
      cnt += 1
      t
    }.toMap
    if(input < 0 || input > cnt - 1) {
      println("Invalid domain number selected, exiting.")
      System.exit(1)
    }

    println("The available domains are: ")
    domainMap.foreach{ tuple =>
      println(s"\t [${tuple._1}] - ${tuple._2}")
    }

    val projectJar = new File("AccessibilityTest.jar")

    val project = Project(projectJar)

    val ai = new BaseAI(true, false)

    // Select a domain for the abstract interpretation
    var domainIdentifierStr = domainMap(input)

    // This is needed because there is likely a bug in the way OPAL 5.0.0 handles domain identifiers.
    domainIdentifierStr = domainIdentifierStr.substring(domainIdentifierStr.indexOf("]") + 2)

    var methodCnt = 0

    // Iterate over all class files in the project
    project.allClassFiles.foreach{ classFile =>

      // Iterate over all methods in the project
      classFile.methods.foreach{ method =>
        // We can only start abstract interpretation when there is code inside the method
        if(method.body.isDefined){
          // Build the domain object
          val domain = DomainRegistry.newDomain(domainIdentifierStr, project, method)

          // Run the actual abstract interpretation for the current method using the interpreter and domain defined above
          val result: AIResult = ai(method, domain)

          val methodCode = result.code
          val evaluatedInstructionPCs = result.evaluatedPCs

          // If the abstract interpretation evaluated (looked at) less instructions than there are in the code, that means
          // there are instructions that are not reachable at all!
          if(evaluatedInstructionPCs.size < methodCode.size){
            println(s"${methodCode.size - evaluatedInstructionPCs.size} instructions not reachable in ${method.fullyQualifiedSignature}")
          }
          println(s"Methode: ${method.name} in Klasse: ${classFile.thisType.simpleName} mit Code Size: ${methodCode.size}")

          //          val rep = XHTML.dump(classFile, method, "Abstract Interpretation Succeeded", result).toString()
          //
          //          Files.writeString(Path.of("ai-data", s"$methodCnt-${method.name.replace("<", "").replace(">", "")}.html"), rep)
          methodCnt += 1

        }
      }
    }
  }

}