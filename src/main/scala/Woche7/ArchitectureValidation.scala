package Woche7

import java.io.File
import io.circe._
import io.circe.generic.semiauto._
import io.circe.jawn.decode
import org.opalj.br.analyses.Project

import java.nio.file.Paths
import scala.collection.mutable
import scala.io.Source

sealed trait RuleType
case object Allowed extends RuleType
case object Forbidden extends RuleType

object RuleType {
  implicit val decoder: Decoder[RuleType] = Decoder[String].emap {
    case "ALLOWED"  => Right(Allowed)
    case "FORBIDDEN" => Right(Forbidden)
    case other      => Left(s"Ungültiger Regeltyp: '$other'. Erlaubt sind 'ALLOWED' oder 'FORBIDDEN'.")
  }
}

case class Rule(
                 from: String,
                 to: String,
                 `type`: RuleType,
                 except: Option[List[Rule]]
               )

object Rule {
  implicit val decoder: Decoder[Rule] = deriveDecoder[Rule]
}

case class Specification(
                          defaultRule: RuleType, //Either["ALLOWED", "FORBIDDEN"],
                          rules: List[Rule]
                        )

object Specification {
  implicit val decoder: Decoder[Specification] = deriveDecoder[Specification]
}

object ArchitectureValidation {
  def main(jsonFile: String, projectFile: String): Unit = {
    //1. JSON einlesen
    val filePath = new File(jsonFile)
    val fileContent: String = {
      val source = Source.fromFile(filePath)
      val content = source.mkString
      source.close()
      content
    }
    //2. Prüfen der JSON
    val result: Either[Error, Specification] = decode[Specification](fileContent)
    result match {
      case Right(spec) =>
        println("JSON erfolgreich geparst:")
        println(s"Default Rule: ${spec.defaultRule}")
        var ruleIndex = 0
        spec.rules.foreach { rule =>
          println(s"  Rule ${ruleIndex + 1}:")
          println(s"    From: ${rule.from}")
          println(s"    To: ${rule.to}")
          println(s"    Type: ${rule.`type`}")
          var exIndex = 0
          rule.except.foreach { exceptions =>
            println("    Exceptions:")
            exceptions.foreach { ex =>
              println(s"      Exception ${exIndex + 1}:")
              println(s"        From: ${ex.from}")
              println(s"        To: ${ex.to}")
              println(s"        Type: ${ex.`type`}")
            }
            exIndex += 1
          }
          ruleIndex += 1
        }
        println("Prüfen, ob Jars und Packages vorhanden sind")
        val project = Project(new File(projectFile))
        result.foreach { spec =>
          spec.rules.foreach { rule =>
            //TODO: Neben Prüfung der Klassennamen müssen auch die Packages und Jar Namen geprüft werden
            //Prüfung der Klassennamen
            if (!project.allClassFiles.exists(cf => cf.fqn.replace("/", ".") == rule.from) ||
              !project.allClassFiles.exists(cf => cf.fqn.replace("/", ".") == rule.to)) {
              println("Die gegebene JSON passt nicht zum Projekt. Vorgang wird abgebrochen")
              System.exit(1)
              //TODO: Hier müssen noch die tatsächlichen Verstöße hin
            }
          }
        }
        println("Die JSON ist Fehlerfrei")
      case Left(error) =>
        println(s"Fehler beim Parsen der JSON: $error")
    }
  }
}