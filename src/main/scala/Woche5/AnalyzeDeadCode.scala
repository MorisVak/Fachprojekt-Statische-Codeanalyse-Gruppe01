package Woche5
import java.io.File
import org.opalj.br.analyses.Project
import org.opalj.ai.AIResult
import org.opalj.ai.BaseAI
import org.opalj.ai.common.DomainRegistry
import org.opalj.br.instructions.Instruction
import org.opalj.collection.BitSet

import java.time.LocalDateTime
import scala.collection.mutable
import scala.io.StdIn

import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Encoder, Json} // für den LocalDateTime-Encoder
import java.io.{BufferedWriter, FileWriter}

object AnalyzeDeadCode {

  def main(args: Array[String]): Unit = {
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
    val programStart = LocalDateTime.now.getNano
    val methodsWithDeadCode: mutable.Set[MethodWithDeadCode] = mutable.Set.empty
    var methodCnt = 0

    allFiles.foreach { fileName =>
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
                method.name,
                deadInstructions
              )
            }
          }
        }
      }
      val timeFinished = LocalDateTime.now
      val programEnd = LocalDateTime.now.getNano
      val programRuntime: Long = (programEnd - programStart) / 1000000
      val dcr = DeadCodeReport(filesAnalyzed, domainIdentifierStr, timeFinished, programRuntime, methodsWithDeadCode)

      val namedJsonReport = NamedJsonReport(s"dead_code_report_${fileName.replace(".jar","")}", dcr)
      saveJsonReport(namedJsonReport)
    }

  }
  private def saveJsonReport(namedJsonReport: NamedJsonReport): Unit = {
    val reportAsJson: Json = namedJsonReport.asJson
    val jsonString = reportAsJson.spaces2 // .spaces2 für Einrückung mit 2 Leerzeichen
    val file = new java.io.File(s"${namedJsonReport.name}.json")
    val writer = new BufferedWriter(new FileWriter(file))
    try {
      writer.write(jsonString)
      println(s"JSON-Report wurde erfolgreich in '${file.getAbsolutePath}' gespeichert.")
    } finally {
      writer.close()
    }
  }
  private case class NamedJsonReport(
                              name: String,
                              resultJson: DeadCodeReport
                            )
}
