package Woche5

//import com.github.nscala_time.time.Imports.{DateTime, Duration, Interval, richDateTime, richReadableInstant}
import org.opalj.ai.{AIResult, BaseAI}
import org.opalj.ai.common.{DomainRegistry, XHTML}
import org.opalj.br.analyses.Project
import play.api.libs.json._
import org.joda.time.{DateTime, Duration}
import org.opalj.br.Method

import java.io.{File, PrintWriter}
import java.nio.file.{Files, Path}
import scala.collection.mutable
import scala.io.Source

object AbstractInterpretation {

  def main(args: Array[String]): Unit = {
    val domainDescriptions = DomainRegistry.domainDescriptions

    val source = Source.fromFile("config.json")
    val content = source.mkString
    source.close()

    println(content)

    val json: JsValue = Json.parse(content)
    val domain = (json \ "domain").as[Int]
    println(domain)

    val jar = (json \ "usedJar").as[String] // Hier ein Array von jars ers
    val ts = (json \ "test").as[List[String]]

    println(jar)
    println(ts)

    var cnt = 0
    val domainMap = domainDescriptions.map{ descr =>
      val t = (cnt, descr)
      cnt += 1
      t
    }.toMap
    if(domain < 0 || domain >= cnt - 1) {
      println("Invalid domain number selected, exiting.")
      System.exit(1)
    }

    println("The available domains are: ")
    domainMap.foreach{ tuple =>
      println(s"\t [${tuple._1}] - ${tuple._2}")
    }
    //set for keeping all the info needed for report
    val foundMethods = mutable.Set.empty[(String, Int, Int, String, mutable.Set[(String, Int)])]

    val processStart : DateTime = DateTime.now()

    ts.foreach { p =>
      val projectJar = new File(p)

      val project = Project(projectJar)

      val ai = new BaseAI(true, false)

      // Select a domain for the abstract interpretation
      var domainIdentifierStr = domainMap(domain)

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
            val deadInstrCount = methodCode.size - evaluatedInstructionPCs.size

            // If the abstract interpretation evaluated (looked at) less instructions than there are in the code, that means
            // there are instructions that are not reachable at all!
            if(evaluatedInstructionPCs.size < methodCode.size){
              println(s"${methodCode.size - evaluatedInstructionPCs.size} instructions not reachable in ${method.fullyQualifiedSignature}")
              val deadInstructions = mutable.Set.empty[(String, Int)]
              methodCode.foreach { instr =>
                if(!evaluatedInstructionPCs.contains(instr.pc)) deadInstructions += ((instr.instruction.toString, instr.pc))
              }
              foundMethods += ((method.fullyQualifiedSignature, methodCode.size, deadInstrCount, classFile.thisType.fqn, deadInstructions ))
            }

            methodCnt += 1

          }
        }
      }
    }

    val processEnd:DateTime = DateTime.now()

    val totalRuntime: Duration = new Duration(processStart, processEnd)
    val totalRuntimeMs: Long = totalRuntime.getMillis

    val pE = processEnd.toString()

    val methodJsons: Seq[JsObject] = foundMethods.toSeq.map {
      case (signature, totalInstr, deadInstr, className, deadInstrs) =>
        val deadInstrJsons = deadInstrs.map {
          case (instr, pc) => Json.obj(
            "stringRepresentation" -> instr,
            "programCounter" -> pc
          )
        }.toSeq

        Json.obj(
          "fullSignature" -> signature,
          "numberOfTotalInstructions" -> totalInstr,
          "numberOfDeadInstructions" -> deadInstr,
          "enclosingTypeName" -> className,
          "deadInstructions" -> deadInstrJsons
        )
    }


    val reportJson: JsObject = Json.obj(
      "filesAnalyzed" -> ts,
      "domainUsed" -> s"$domain",
      ("timeFinished" -> pE),
      "totalRuntimeMs" -> totalRuntimeMs,
      "methodsFound" -> methodJsons
    )

    val jsonString = Json.prettyPrint(reportJson)

    val writer = new PrintWriter("report.json")
    try writer.write(jsonString)
    finally writer.close()

  }

}
