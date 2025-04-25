import org.opalj.br
import org.opalj.br.analyses.Project
import org.opalj.da.ClassFileReader.{ClassFile, attributeType, fieldInfoType}
import org.opalj.da.{Fields, Methods}

import scala.io.StdIn

object godclass {

  def main(args: Array[String]): Unit = {
    val jarFile = new java.io.File("pdfbox-2.0.24.jar")

    println("-----------GOD CLASS ANALYZER-----------")

    //getting the input of the user
    println("please enter fields threshold")
    val fieldsThreshold = StdIn.readLine().toInt
    println(s"You entered ${fieldsThreshold}")
    println("please enter a methods threshold")
    val methodsThreshold = StdIn.readLine().toInt
    println(s"You entered ${methodsThreshold}")
    val project = Project(jarFile)
    println(s"Total amount of classes in pdfbox-2.0.24.jar :  ${project.classFilesCount}")
    val classFiles = project.allClassFiles
    //Iterate over all the classes
    classFiles.foreach { classFile =>
      //Count amount of fields
      val countFields = totalFields(classFile.fields)
      /*classFile.fields.foreach{field =>
          println(s"FIELD NAME : ${field.name} FIELD TYPE : ${field.fieldType} " +
            s"IS BASE TYPE : ${field.fieldType.isBaseType} IS REFERENCE TYPE : ${field.fieldType.isReferenceType}")
        }*/
      val yes = checkAndCountForeignFieldTypes(classFile.fields, classFile)
      //count amount of methods
      val countMethods = totalClasses(classFile.methods)
      //val totalLoc = 0
      println(s"Class TYPE = ${classFile.thisType}")

      if (countFields > fieldsThreshold) {
        println("CLASS EXCEEDED FIELDS THRESHOLD = might be a god class")
        println(s"Fields Threshold = ${fieldsThreshold}")
        println(s"Count of FIELDS for Class ${classFile} = ${countFields} ")
      }
      if (countMethods > methodsThreshold) {
        println("CLASS EXCEEDED METHODS THRESHOLD = might be a god class")
        println(s"Methods Threshold = ${methodsThreshold}")
        println(s"Count of METHODS for Class ${classFile} = ${countMethods} ")
      }
    }
    println(s"Project Class File Count ${project.classFilesCount}")
  }

  def totalClasses(methods: br.Methods): Int = {
    val count = methods.count(method => true)
    return count
  }

  def totalFields(fields: br.Fields): Int = {
    val count = fields.count(field => true)
    fields.foreach { field =>
      println(s"FIELD NAME : ${field.name} FIELD TYPE : ${field.fieldType} " +
        s"IS BASE TYPE : ${field.fieldType.isBaseType} IS REFERENCE TYPE : ${field.fieldType.isReferenceType}")
    }
    return count
  }

  def checkAndCountForeignFieldTypes (fields: br.Fields, fieldClass : org.opalj.br.ClassFile): Int = {
    var count = 0

    fieldClass.thisType
    fields.foreach{field =>
      if(field.fieldType.isReferenceType){
        if(!field.fieldType.toString.contains("java/") && !field.fieldType.isBaseType && !field.fieldType.isArrayType){
          if(fieldClass.thisType.toString != field.fieldType.toString){
            println("YUP")

            count += 1
          }
          println(s"DIFFERENT OBJECT : ${field.fieldType}")
        }
      }
    }
    return count
  }
}

/*if(field.fieldType.isReferenceType){
  if(!field.fieldType.toString.contains("java/") && !field.fieldType.isBaseType && !field.fieldType.isArrayType){
    println(s"GOTCHA : ${field.fieldType}")
  }
}*/