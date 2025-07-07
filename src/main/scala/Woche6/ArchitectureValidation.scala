package Woche6

import java.io.File
import io.circe._
import io.circe.generic.semiauto._
import io.circe.jawn.decode
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

object ArchitectureValidation extends App {
  //1. JSON einlesen
  private val filePath = new File("ArchitectureData.json")
  private val fileContent: String = {
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
      val ruleIndex = 1
      spec.rules.foreach { rule =>
        println(s"  Rule ${ruleIndex + 1}:")
        println(s"    From: ${rule.from}")
        println(s"    To: ${rule.to}")
        println(s"    Type: ${rule.`type`}")
        val exIndex = 1
        rule.except.foreach { exceptions =>
          println("    Exceptions:")
          exceptions.foreach { ex =>
            println(s"      Exception ${exIndex + 1}:")
            println(s"        From: ${ex.from}")
            println(s"        To: ${ex.to}")
            println(s"        Type: ${ex.`type`}")
          }
        }
      }
    case Left(error) =>
      println(s"Fehler beim Parsen der JSON: $error")
  }
}