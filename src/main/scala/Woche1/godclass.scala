import org.opalj.br.BaseType
import org.opalj.br.analyses.Project

import scala.io.StdIn.{readInt, readLine}

object godclasstest {

  def main(args : Array[String]): Unit = {
    val jarFile = new java.io.File("pdfbox-2.0.24.jar")
    val project = Project(jarFile)

    var godClassCount = 0

    // Average fields and methods per class
    val allFieldsCount = project.allFields.size
    val allMethodsCount = project.allMethods.size
    if (project.allClassFiles.nonEmpty) {
      val fieldAvg = allFieldsCount.toDouble / project.allClassFiles.size
      val methodAvg = allMethodsCount.toDouble / project.allClassFiles.size
      println(s"Die durchschnittliche Feld Anzahl pro Klasse ist: $fieldAvg ")
      println(s"Die durchschnittliche Methoden Anzahl pro Klasse ist: $methodAvg ")
    } else {
      println("Keine Klassen gefunden.")
    }

    // Limit Inputs - configurable
    println("Schwellenwert für Felder Anzahl eingeben: ")
    val fieldLimit = readInt()

    println("Schwellenwert für statische Felder eingeben: ")
    val staticFieldLimit = readInt()

    println("Schwellenwert für klassenfremde Felder: ")
    val foreignFieldsLimit = readInt()

    println("Schwellenwert für Methoden Anzahl eingeben: ")
    val methodLimit = readInt()
    //

    project.allClassFiles.foreach { classFile =>

      val fieldSize = classFile.fields.size
      val staticFieldCount = classFile.fields.count(_.isStatic)
      val methodCount = classFile.methods.size
      val foreignFieldCount = classFile.fields.count { field =>
        val fieldType = field.fieldType
        fieldType.isReferenceType && fieldType != classFile.thisType // checks if a field references a diff. object
      }
      // Limit Checks for a classFile
      if (fieldSize >= fieldLimit ||
        staticFieldCount >= staticFieldLimit ||
        methodCount >= methodLimit ||
        foreignFieldCount >= foreignFieldsLimit) {
        godClassCount += 1 // (Potential) God Class, thus printed out with name and values
        println(s"Class: ${classFile.fqn}, Fields: $fieldSize, Static Fields: $staticFieldCount , Foreign Fields: $foreignFieldCount, Methods: $methodCount")
      }
    }
    // Count of god classes in the project
    println(s"Insgesamt gibt es $godClassCount mögliche Gott klassen.")
    }

}