package Woche6


import java.io.File
import io.circe._
import io.circe.generic.semiauto._
import io.circe.jawn.decode
import org.opalj.br.analyses.Project
import org.opalj.br.instructions.MethodInvocationInstruction

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

object ArchitectureValidation extends App {

  val filesToBenchmark = new File("BenchmarkTwoJars")
  val project = Project(filesToBenchmark)
  println("!!!!")
  project.packages.foreach(pack => {println(pack)})

  //extracting files
  val files = filesToBenchmark.listFiles().toList

  val fileNames = files.map(f => f.getName)

  println(fileNames)


  //1. JSON einlesen
  private val filePath = new File("ArchitectureBenchmarkTwo.json")
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

      result.foreach { spec =>
        spec.rules.foreach { rule =>
          //TODO: Neben Prüfung der Klassennamen müssen auch die Packages und Jar Namen geprüft werden

          //Prüfugn der Jars
          /*if (rule.from.contains(".jar") && !fileNames.contains(rule.from) ||
            rule.to.contains(".jar") && !fileNames.contains(rule.to)){
            println("Die gegebene JSON passt nicht zum Projekt. Vorgang wird abgebrochen")
            System.exit(1)
          }*/
          //Prüfung der packages
          /*if(!project.packages.exists(pack => pack.replace("/",".") == rule.from) ||
            !project.packages.exists(pack => pack.replace("/",".") == rule.to)){
            println("Die gegebene JSON passt nicht zum Projekt. Vorgang wird abgebrochen")
            System.exit(1)
          }*/

          //Prüfung der Klassennamen
          /*if(!project.allClassFiles.exists(cf => cf.fqn.replace("/",".") == rule.from ) ||
            !project.allClassFiles.exists(cf => cf.fqn.replace("/",".") == rule.to )){
            println("Die gegebene JSON passt nicht zum Projekt. Vorgang wird abgebrochen")
            System.exit(1)
            //TODO: Hier müssen noch die tatsächlichen Verstöße hin
          }*/
        }
      }
      println("Die JSON ist Fehlerfrei")
    case Left(error) =>
      println(s"Fehler beim Parsen der JSON: $error")
  }

  //analysis
  val resultSet = mutable.Set[String]()

  val classFiles = project.allClassFiles
  classFiles.foreach{ classFile =>
    //println(s"---- ${classFile.fqn}")
    val methods  = classFile.methods
    methods.foreach{method =>
      val body = method.body
      body.foreach{
        line => line.instructions.foreach{
          case invokedMethod: MethodInvocationInstruction =>
            val notAllowedFlag = true
            /**
             * Plan --> go through rules and check for the package.
             * IF FORBIDDEN -> add to result, notAllowedFlag = true
             * ELSE -> iterate over exceptions
             *    IF METHOD IS FOUND AND FORBIDDEN -> add to result, notAllowedFlag = true
             *    ELSE IF METHOD IS FOUND AND ACCEPTED -> break
             *
             *
             *    s"${method.classFile.fqn.replace("/",".")} \n is not allowed to access : \n ${invokedMethod.declaringClass.toJava} \n"
             * */

            //IF THE METHOD USED IS NOT FROM THE SAME CLASS
            if(method.classFile.fqn.replace("/",".") != invokedMethod.declaringClass.toJava &&
              !invokedMethod.declaringClass.toJava.contains("java.")){
              //Get package names
              var invokedPackage = ""
                project.packages.foreach( pack => {
                if (invokedMethod.declaringClass.toJava.startsWith(pack.replace("/","."))){
                  invokedPackage = pack.replace("/",".")
                }
              })
              var methodPackage = ""
              project.packages.foreach(pack =>
                if (method.classFile.fqn.replace("/",".").startsWith(pack.replace("/","."))){
                  methodPackage = pack.replace("/",".")
                })


              if(invokedPackage != methodPackage){
                //println("FOUND DIFFERENCE")
                //println(methodPackage)
                //println(invokedPackage)

                result.foreach(spec => {

                  if(!spec.rules.exists( rule => rule.from == methodPackage && rule.to == invokedPackage)){
                    resultSet += s" WARNING PACKAGE : $methodPackage \n is not allowed to access PACKAGE : \n $invokedPackage \n"
                  }
                  spec.rules.foreach( rule => {
                    //convert jar to corresponding package
                    if(rule.to.contains(".jar")){
                      val splitJar = rule.to.split('.').dropRight(1).mkString(".")
                      var toPackage = ""
                      project.packages.foreach(pack => if (pack.contains(splitJar)){
                        toPackage = pack
                      })
                    }else if (rule.from.contains(".jar")){
                      val splitJar = rule.from.split('.').dropRight(1).mkString(".")
                      var toPackage = ""
                      project.packages.foreach(pack => if (pack.contains(splitJar)){
                        toPackage = pack
                      })
                    }

                    //check if package is allowed to use other package
                    if(methodPackage == rule.from && invokedPackage == rule.to){
                      rule.except match {
                        case Some(exceptions) => exceptions.foreach{ex =>
                          if(ex.from.contains(method.classFile.fqn.replace("/","."))&&
                            ex.to.contains(invokedMethod.declaringClass.toJava) &&
                            ex.`type`.toString == "Forbidden"){
                          }

                          resultSet += s"WARNING CLASS : ${ex.from} \n is not allowed to access CLASS : \n ${ex.to} \n"
                        }
                        case None =>
                      }
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
  resultSet.foreach(entry => println(entry))

}