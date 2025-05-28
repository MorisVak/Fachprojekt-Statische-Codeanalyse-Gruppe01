package Woche3

import org.opalj.br
import org.opalj.br.analyses.Project

import scala.collection.mutable
import scala.io.StdIn

object godclass {

  def main(args: Array[String]): Unit = {
    val jarFile = new java.io.File("pdfbox-2.0.24.jar")

    println("-----------GOD CLASS ANALYZER-----------\n")

    //getting the input of the user
    println("please enter fields threshold : ")
    val fieldsThreshold = StdIn.readLine().toInt
    println(s"You entered ${fieldsThreshold}")
    println("please enter a methods threshold : ")
    val methodsThreshold = StdIn.readLine().toInt
    println(s"You entered ${methodsThreshold}")
    println("please enter a fields of different objects thershold : ")
    val differentClassFieldsThreshold = StdIn.readLine().toInt
    println(s"You entered ${differentClassFieldsThreshold}")

    //Result map that saves all potential god classes
    val results = mutable.Map[String, Array[String]]()

    //Count of all potential god classes
    var godClassCount = 0


    val project = Project(jarFile)
    println(s"Total amount of classes in pdfbox-2.0.24.jar :  ${project.classFilesCount}")
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
      println(s"Class TYPE = ${classFile.thisType}")

      if (countFields > fieldsThreshold) {
        println("CLASS EXCEEDED FIELDS THRESHOLD = might be a god class")
        println(s"Fields Threshold = ${fieldsThreshold}")
        println(s"Count of FIELDS for Class ${classFile} = ${countFields} ")
        // Get the current array (or an empty array if missing)
        val currentArray = results.getOrElse(classFile.toString(), Array[String]())

        // Create a new array by concatenating the new value
        val updatedArray = currentArray :+ s"Field Count EXCEEDED : ${countFields} instead of ${fieldsThreshold}"

        // Update the map entry
        results.update(classFile.toString(), updatedArray)
      }

      if (countMethods > methodsThreshold) {
        println("CLASS EXCEEDED METHODS THRESHOLD = might be a god class")
        println(s"Methods Threshold = ${methodsThreshold}")
        println(s"Count of METHODS for Class ${classFile} = ${countMethods} ")
        // Get the current array (or an empty array if missing)
        val currentArray = results.getOrElse(classFile.toString(), Array[String]())

        // Create a new array by concatenating the new value
        val updatedArray = currentArray :+ s"Method Count EXCEEDED : ${countMethods} instead of ${methodsThreshold}"

        // Update the map entry
        results.update(classFile.toString(), updatedArray)
      }
      if(countDifferentObjectFields > differentClassFieldsThreshold){
        println("CLASS EXCEEDEDS DIFFERENT OBJECT FIELDS = might be a god class")
        println(s"Different Object Threshold = ${differentClassFieldsThreshold}")
        println(s"Count of Different Objects for Class ${classFile} = ${countDifferentObjectFields} ")
        // Get the current array (or an empty array if missing)
        val currentArray = results.getOrElse(classFile.toString(), Array[String]())

        // Create a new array by concatenating the new value
        val updatedArray = currentArray :+ s"Different Object Count EXCEEDED : ${countDifferentObjectFields} instead of" +
          s" ${differentClassFieldsThreshold}"

        // Update the map entry
        results.update(classFile.toString(), updatedArray)
      }
    }
    println(s"\nProject Class File Count ${project.classFilesCount}")
    println("-----------RESULT-----------")
    results.foreach { case (key, array) =>
      // Only print if array is not empty
      if (array.nonEmpty) {
        godClassCount += 1
        println(s"\n$key:")
        array.foreach(value => println(s"  - $value"))
        println("_____________________________________________________")
      }
    }

    println(s"\nTotal potential god classes : ${godClassCount} of ${project.classFilesCount} ")
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
            println(s"TYPE ________   ${field.fieldType} _____________")

            count += 1
          }
      }
    }
    return count
  }
}