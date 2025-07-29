package Woche7

import org.opalj.br
import org.opalj.br.analyses.Project
import java.io.PrintWriter

import java.io.{File, FileOutputStream, PrintStream}
import scala.collection.mutable
import scala.io.StdIn

object godclass {

  def main(fieldsThreshold: Int, methodsThreshold: Int, differentClassFieldsThreshold: Int, jarPath: String): Unit = {
    val jarFile = new java.io.File(jarPath)
    var outputString = ""

    println("-----------GOD CLASS ANALYZER-----------\n")
    outputString += "-----------GOD CLASS ANALYZER-----------\n \n "

    //Result map that saves all potential god classes
    val results = mutable.Map[String, Array[String]]()

    //Count of all potential god classes
    var godClassCount = 0


    val project = Project(jarFile)
    //println(s"Total amount of classes in pdfbox-2.0.24.jar :  ${project.classFilesCount}")
    val classFiles = project.allClassFiles
    //Iterate over all the classes



      classFiles.foreach { classFile =>
        results += (classFile.fqn -> Array.empty[String])
        //Count amount of fields
        val countFields = totalFields(classFile.fields)
        val countDifferentObjectFields = checkAndCountForeignFieldTypes(classFile.fields, classFile)
        //count amount of methods
        val countMethods = totalClasses(classFile.methods)
        //val totalLoc = 0

        if (countFields > fieldsThreshold) {
          println("CLASS EXCEEDED FIELDS THRESHOLD = might be a god class")
          println(s"Fields Threshold = ${fieldsThreshold}")
          println(s"Count of FIELDS for Class ${classFile} = ${countFields} ")
          outputString +=s"CLASS EXCEEDED FIELDS THRESHOLD = " +
            s"might be a god class \n Fields Threshold = ${fieldsThreshold} " +
            s"\n Count of FIELDS for Class ${classFile} = ${countFields} \n "
          // Get the current array (or an empty array if missing)
          val currentArray = results.getOrElse(classFile.toString(), Array[String]())

          // Create a new array by concatenating the new value
          val updatedArray = currentArray :+ s"Field Count EXCEEDED : ${countFields} instead of ${fieldsThreshold}"
          outputString += s"Field Count EXCEEDED : ${countFields} instead of ${fieldsThreshold} \n"
          // Update the map entry
          results.update(classFile.fqn, updatedArray)
        }

        if (countMethods > methodsThreshold) {
          println("CLASS EXCEEDED METHODS THRESHOLD = might be a god class")
          println(s"Methods Threshold = ${methodsThreshold}")
          println(s"Count of METHODS for Class ${classFile} = ${countMethods} ")
          outputString += s"CLASS EXCEEDED METHODS THRESHOLD = might be a god class \n " +
            s"Methods Threshold = ${methodsThreshold} \n " +
            s"Count of METHODS for Class ${classFile} = ${countMethods} \n"
          // Get the current array (or an empty array if missing)
          val currentArray = results.getOrElse(classFile.toString(), Array[String]())

          // Create a new array by concatenating the new value
          val updatedArray = currentArray :+ s"Method Count EXCEEDED : ${countMethods} instead of ${methodsThreshold}"
          outputString += s"Method Count EXCEEDED : ${countMethods} instead of ${methodsThreshold} \n"

          // Update the map entry
          results.update(classFile.fqn, updatedArray)
        }
        if (countDifferentObjectFields > differentClassFieldsThreshold) {
          println("CLASS EXCEEDEDS DIFFERENT OBJECT FIELDS = might be a god class")
          println(s"Different Object Threshold = ${differentClassFieldsThreshold}")
          println(s"Count of Different Objects for Class ${classFile} = ${countDifferentObjectFields} ")
          // Get the current array (or an empty array if missing)
          val currentArray = results.getOrElse(classFile.toString(), Array[String]())

          // Create a new array by concatenating the new value
          val updatedArray = currentArray :+ s"Different Object Count EXCEEDED : ${countDifferentObjectFields} instead " +
            s"of ${differentClassFieldsThreshold}"
          outputString += s"CLASS EXCEEDEDS DIFFERENT OBJECT FIELDS = might be a god class \n " +
            s"Different Object Threshold = ${differentClassFieldsThreshold} \n " +
            s"Count of Different Objects for Class ${classFile} = ${countDifferentObjectFields}  \n " +
            s"Different Object Count EXCEEDED : ${countDifferentObjectFields} instead of ${differentClassFieldsThreshold} \n"

          // Update the map entry
          results.update(classFile.toString(), updatedArray)
        }
      }
      println("-----------RESULT-----------")
    outputString += s"-----------RESULT----------- \n"
      results.foreach { case (key, array) =>
        // Only print if array is not empty
        if (array.nonEmpty) {
          godClassCount += 1
          println(s"\n$key:")
          outputString += s"$key: \n "
          array.foreach(value => {
            println(s"  - $value")
            outputString += s"  - $value \n"
          })
          println("_____________________________________________________")
          outputString += s"_____________________________________________________\n"
        }
      }
      println(s"\nTotal potential god classes : ${godClassCount} of ${project.classFilesCount} ")
    outputString += s"Total potential god classes : ${godClassCount} of ${project.classFilesCount}"

    println("-----------------------------------------------\n")

    val printer = new PrintWriter(" Results/godclass_result.txt")
    printer.write(outputString)
    printer.close()
  }

  def totalClasses(methods: br.Methods): Int = {
    val count = methods.size
    return count
  }

  def totalFields(fields: br.Fields): Int = {
    val count = fields.size
    return count
  }

  def checkAndCountForeignFieldTypes (fields: br.Fields, fieldClass : org.opalj.br.ClassFile): Int = {
    var count = 0

    fields.foreach{field =>
        if(!field.fieldType.toString.contains("java/") && field.fieldType.toString.contains("ObjectType")){
          if(fieldClass.thisType.toString != field.fieldType.toString){

            count += 1
          }
      }
    }
    return count
  }
}