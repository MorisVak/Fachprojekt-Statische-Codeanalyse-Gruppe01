package Woche7

import java.io.File
import io.circe._
import io.circe.generic.semiauto._
import io.circe.jawn.decode
import org.opalj.br.analyses.Project
import java.io.PrintWriter

import java.nio.file.Paths
import scala.collection.mutable
import scala.io.Source


import java.io.File
import io.circe._
import io.circe.generic.semiauto._
import io.circe.jawn.decode
import org.opalj.br.analyses.Project
import org.opalj.br.instructions.MethodInvocationInstruction

import java.nio.file.Paths
import scala.collection.convert.ImplicitConversions.`collection asJava`
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
  var outputString = ""
  def main(jsonFile: String, projectFile: String): Unit = {
    println("-----------ARCHITECTURE ANALYZER-----------\n")
    outputString += "-----------ARCHITECTURE ANALYZER-----------\n \n"
    val filesToBenchmark = new File(projectFile)
    var continueAnalysis = true
    val project = Project(filesToBenchmark)
    outputString += "PACKAGES TO ANALYZE : \n"
    project.packages.foreach(pack => {
    outputString += s" - $pack \n"})

    //extracting files
    val files = filesToBenchmark.listFiles().toList

    val fileNames = files.map(f => f.getName)


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
        outputString += s" ________________________________________ \n" +
          s" - Default Rule: ${spec.defaultRule} - \n"
        var ruleIndex = 0
        spec.rules.foreach { rule =>
          println(s"  Rule ${ruleIndex + 1}:")
          println(s"    From: ${rule.from}")
          println(s"    To: ${rule.to}")
          println(s"    Type: ${rule.`type`}")
          var exIndex = 0
          outputString += s"  Rule ${ruleIndex + 1}: \n    From: ${rule.from} \n    To: ${rule.to}     Type: ${rule.`type`}" +
            s" \n \n"
          rule.except.foreach { exceptions =>
            println("    Exceptions:")
            outputString+= "    Exceptions: \n"
            exceptions.foreach { ex =>
              println(s"      Exception ${exIndex + 1}:")
              println(s"        From: ${ex.from}")
              println(s"        To: ${ex.to}")
              println(s"        Type: ${ex.`type`}")
              outputString+= s"      Exception ${exIndex + 1}: \n         From: ${ex.from} \n        To: ${ex.to} \n         Type: ${ex.`type`}\n"
            }
            exIndex += 1
          }
          ruleIndex += 1
        }

        result.foreach { spec =>
          val allPackages = project.packages.map(_.replace("/", "."))
          val allClasses = project.allClassFiles.map(_.fqn.replace("/", "."))

          def existsAsPackageOrClass(name: String): Boolean = {
            allPackages.contains(name) || allClasses.contains(name)
          }

          spec.rules.foreach { rule =>

            // Validate `from` and `to` in the main rule
            if(rule.from.contains(".jar") && rule.to.contains(".jar")){
              if(!fileNames.contains(rule.from) || !fileNames.contains(rule.to)){
                println(s"Ungültiges JAR 'from' oder 'to' in Regel:\n  from: ${rule.from}\n  to: ${rule.to}")
                outputString += s"Ungültiges JAR 'from' oder 'to' in Regel:\n  from: ${rule.from}\n  to: ${rule.to} \n"
                continueAnalysis = false
              }
            }else if (rule.from.contains(".jar") && !rule.to.contains(".jar")){
              if(!fileNames.contains(rule.from) || !existsAsPackageOrClass(rule.to)){
                println(s"Ungültiges 'from' oder 'to' in Regel:\n  from: ${rule.from}\n  to: ${rule.to}")
                outputString += s"Ungültiges 'from' oder 'to' in Regel:\n  from: ${rule.from}\n  to: ${rule.to} \n"
                continueAnalysis = false
              }
            }else if (rule.to.contains(".jar") && !rule.from.contains(".jar")){
              if(!fileNames.contains(rule.to) || !existsAsPackageOrClass(rule.from)){
                println(s"Ungültiges 'from' oder 'to' in Regel:\n  from: ${rule.from}\n  to: ${rule.to}")
                outputString += s"Ungültiges 'from' oder 'to' in Regel:\n  from: ${rule.from}\n  to: ${rule.to} \n"
                continueAnalysis = false
              }
            }else{
              if (!existsAsPackageOrClass(rule.from) || !existsAsPackageOrClass(rule.to)) {
                println(s"Ungültiges 'from' oder 'to' in Regel:\n  from: ${rule.from}\n  to: ${rule.to}")
                outputString += s"Ungültiges 'from' oder 'to' in Regel:\n  from: ${rule.from}\n  to: ${rule.to} \n"
                continueAnalysis = false
              }

              // Validate exceptions (if any)
              rule.except.getOrElse(Nil).foreach { ex =>
                if (!existsAsPackageOrClass(ex.from) || !existsAsPackageOrClass(ex.to)) {
                  println(s"Ungültiges 'from' oder 'to' in Ausnahme:\n  from: ${ex.from}\n  to: ${ex.to}")
                  outputString += s"Ungültiges 'from' oder 'to' in Ausnahme:\n  from: ${rule.from}\n  to: ${rule.to} \n"
                  continueAnalysis = false
                }
              }
            }
          }
        }
        println("Die JSON ist Fehlerfrei")
      case Left(error) =>
        println(s"Fehler beim Parsen der JSON: $error")
    }

    //analysis
    if(continueAnalysis) {
      val resultSet = mutable.Set[String]()
      val classFiles = project.allClassFiles
      classFiles.foreach { classFile =>
        //println(s"---- ${classFile.fqn}")
        val methods = classFile.methods
        methods.foreach { method =>
          val body = method.body
          body.foreach {
            line =>
              line.instructions.foreach {
                case invokedMethod: MethodInvocationInstruction =>
                  val notAllowedFlag = true

                  //IF THE METHOD USED IS NOT FROM THE SAME CLASS
                  if (method.classFile.fqn.replace("/", ".") != invokedMethod.declaringClass.toJava &&
                    !invokedMethod.declaringClass.toJava.contains("java.")) {
                    //Get package names
                    var invokedPackage = ""
                    project.packages.foreach(pack => {
                      if (invokedMethod.declaringClass.toJava.startsWith(pack.replace("/", "."))) {
                        invokedPackage = pack.replace("/", ".")
                      }
                    })
                    var methodPackage = ""
                    project.packages.foreach(pack =>
                      if (method.classFile.fqn.replace("/", ".").startsWith(pack.replace("/", "."))) {
                        methodPackage = pack.replace("/", ".")
                      })


                    if (invokedPackage != methodPackage) {
                      result.foreach(spec => {

                        if (!spec.rules.exists(rule => rule.from == methodPackage && rule.to == invokedPackage)) {
                          resultSet += s" WARNING PACKAGE : $methodPackage \n is not allowed to access PACKAGE : \n $invokedPackage \n"
                          outputString += s" WARNING PACKAGE : $methodPackage \n is not allowed to access PACKAGE : \n $invokedPackage \n"
                        }
                        spec.rules.foreach(rule => {
                          //convert jar to corresponding package
                          if (rule.to.contains(".jar")) {
                            val splitJar = rule.to.split('.').dropRight(1).mkString(".")
                            var toPackage = ""
                            project.packages.foreach(pack => if (pack.contains(splitJar)) {
                              toPackage = pack
                            })
                          } else if (rule.from.contains(".jar")) {
                            val splitJar = rule.from.split('.').dropRight(1).mkString(".")
                            var toPackage = ""
                            project.packages.foreach(pack => if (pack.contains(splitJar)) {
                              toPackage = pack
                            })
                          }

                          //check if package is allowed to use other package
                          //RULE IST NOT ALLOWING PACKAGE TO PACKAGE
                          if (methodPackage == rule.from && invokedPackage == rule.to) {
                            rule.except match {
                              case Some(exceptions) => exceptions.foreach { ex =>
                                if (ex.from.contains(method.classFile.fqn.replace("/", ".")) &&
                                  ex.to.contains(invokedMethod.declaringClass.toJava) &&
                                  ex.`type`.toString == "Forbidden") {
                                }

                                resultSet += s"WARNING CLASS : ${ex.from} \n is not allowed to access CLASS : \n ${ex.to} \n"
                              }
                              case None =>
                            }
                            //base rule compares two classes
                          } else if (rule.from == method.classFile.fqn.replace("/", ".") &&
                            rule.to == invokedMethod.declaringClass.toJava && rule.`type`.toString == "Forbidden") {
                            resultSet += s"WARNING CLASS : ${rule.from} \n is not allowed to access CLASS : \n ${rule.to} \n"
                          }
                        })
                      })
                    }
                  }
                case _ =>
              }
          }
        }
      }
      resultSet.foreach(entry => {
        println(entry)
        outputString += s"$entry"
      })
      val pw = new PrintWriter(" Results/architechtureValidation_result.txt")
      pw.write(outputString)
      pw.close()
    }else{
      println("ANALYSIS TERMINATED CHECK ABOVE FOR ERROR")
      val pw = new PrintWriter(" Results/architechtureValidation_result.txt")
      pw.write(outputString)
      pw.close()
    }
    println("-------------------------------------------\n")
  }
}