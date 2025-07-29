package Woche7

import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.joda.time.{DateTime, Duration}
import org.opalj.ai.{AIResult, BaseAI}
import org.opalj.ai.common.DomainRegistry
import org.opalj.br.analyses.Project
import org.opalj.br.instructions.Instruction
import org.opalj.collection.BitSet

import java.io._
import java.net.{HttpURLConnection, URL}
import java.time.LocalDateTime
import scala.collection.mutable

object AnalyzeDeadCode {

  def main(): Unit = {
    println("-----------DEAD CODE ANALYZER-----------\n")
    // *** KORREKTUR: Erstelle eine neue Instanz der Dialog-Klasse ***
    val userInputOpt = new InputWindow().showAndGetInput()
    val allFiles: mutable.Set[String] = mutable.Set.empty
    // Der Rest der Methode bleibt exakt gleich
    userInputOpt match {
      case Some(input) =>
        println("\n--- GUI-Eingabe erhalten. Starte Analyse... ---")
        println(s"Gewählter Domain-Index: ${input.someNumber}")
        println("Dateien:")
        input.filePaths.foreach(file => allFiles += file)
        runAnalysis(input.someNumber, allFiles)

      case None =>
        println("\nAnalyse vom Benutzer abgebrochen.")
    }
  }

  private def runAnalysis(domainIndex: Int, allFiles: mutable.Set[String]): Unit = {
    val domainDescriptions = DomainRegistry.domainDescriptions
    var cnt = 0
    val domainMap = domainDescriptions.map { descr =>
      val t = (cnt, descr)
      cnt += 1
      t
    }.toMap


    val ai = new BaseAI(true, false)

    var domainIdentifierStr = domainMap(domainIndex)

    domainIdentifierStr = domainIdentifierStr.substring(domainIdentifierStr.indexOf("]") + 2)
    val methodsWithDeadCode: mutable.Set[MethodWithDeadCode] = mutable.Set.empty
    var methodCnt = 0

    allFiles.foreach { fileName =>
      val programStart = DateTime.now()
      val project = Project(new File(fileName))
      val filesAnalyzed: List[String] = project.allClassFiles.map(_.fqn).toList
      project.allClassFiles.foreach { classFile =>
        classFile.methods.foreach { method =>
          if (method.body.isDefined) {
            methodCnt += 1
            val domain = DomainRegistry.newDomain(domainIdentifierStr, project, method)
            println(method.name)
            val result: AIResult = ai(method, domain)
            val methodCode = result.code
            val evaluatedInstructionPCs = result.evaluatedPCs

            if (evaluatedInstructionPCs.size < methodCode.size) {
              println(s"${methodCode.size - evaluatedInstructionPCs.size} instructions not reachable in ${method.fullyQualifiedSignature}")
              val evaluated: BitSet = result.evaluatedInstructions
              val instructions: Seq[Instruction] = methodCode.instructions
              val allSet: mutable.Set[Instruction] = mutable.Set(instructions: _*)
              val deadInstructions: mutable.Set[DeadInstruction] = mutable.Set.empty
              evaluated.iterator.foreach { idx =>
                if (idx >= 0 && idx < instructions.length) {
                  allSet -= instructions(idx)
                }
              }
              allSet.foreach { deadInstruction =>
                if (deadInstruction != null) {
                  deadInstructions += DeadInstruction(deadInstruction.toString, methodCnt)
                }
              }

              methodsWithDeadCode += MethodWithDeadCode(
                method.fullyQualifiedSignature,
                method.body.get.instructions.length,
                methodCode.size - evaluatedInstructionPCs.size,
                //TODO: Enclosing irgendwas
                classFile.thisType.fqn,
                deadInstructions
              )
            }
          }
        }
      }

      // 0 3 5 10
      val timeFinished = LocalDateTime.now
      val programEnd = DateTime.now()

      val totalRuntime: Duration = new Duration(programStart, programEnd)
      val programRuntime: Long = totalRuntime.getMillis
      val dcr = DeadCodeReport(allFiles.toList, domainIdentifierStr, timeFinished, programRuntime, methodsWithDeadCode)

      val namedJsonReport = NamedJsonReport(s"dead_code_report_domain_${domainIndex}_${fileName.replace(".jar","")}", dcr)
      saveJsonReport(namedJsonReport)
    }

  }
  private def saveJsonReport(namedJsonReport: NamedJsonReport): Unit = {
    val reportAsJson: Json = namedJsonReport.asJson
    val jsonString = reportAsJson.spaces2
    val file = new java.io.File(s"${namedJsonReport.name}.json")
    val writer = new BufferedWriter(new FileWriter(file))
    try {
      writer.write(jsonString)
      println(s"JSON-Report wurde erfolgreich in '${file.getAbsolutePath}' gespeichert.")
    } finally {
      writer.close()
    }
    println("----------------------------------------\n")
    val writeToDb = false
    if (writeToDb) {

      try {
        val jsonData = namedJsonReport.asJson.noSpaces
        val url = new URL("https://exc05.onrender.com/api/exc-results")
        val connection = url.openConnection().asInstanceOf[HttpURLConnection]

        connection.setRequestMethod("POST")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setDoOutput(true)

        // Send JSON data
        val writer = new OutputStreamWriter(connection.getOutputStream)
        writer.write(jsonData)
        writer.flush()
        writer.close()

        // Get response
        val responseCode = connection.getResponseCode
        println(s"Java HTTP - Status: $responseCode")

        if (responseCode >= 200 && responseCode < 300) {
          val reader = new BufferedReader(new InputStreamReader(connection.getInputStream))
          val response = reader.lines().toArray.mkString("\n")
          reader.close()
          println(s"Java HTTP - Success: $response")
        } else {
          val errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream))
          val errorResponse = errorReader.lines().toArray.mkString("\n")
          errorReader.close()
          println(s"Java HTTP - Error: $errorResponse")
        }

      } catch {
        case e: Exception =>
          println(s"Java HTTP failed: ${e.getMessage}")
          e.printStackTrace()
      }
    }
  }
  private case class NamedJsonReport(
                              name: String,
                              resultJson: DeadCodeReport
                            )
}
