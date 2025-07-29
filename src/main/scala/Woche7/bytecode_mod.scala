package Woche7

import org.opalj.ba.toDA
import org.opalj.bc.Assembler
import org.opalj.br.analyses.Project
import org.opalj.br.instructions.Instruction
import org.opalj.br.{ClassFile, Method, NoExceptionHandlers}

import java.io.File
import java.nio.file.{Files, Paths}
import scala.collection.mutable
import scala.io.Source

object bytecode_mod {
  println("-----------FORBIDDEN METHODS ANALYZER-----------\n")
  private val forbiddenMethods: mutable.Map[String, mutable.Set[String]] = mutable.Map.empty //= list.map(k => k -> mutable.Set.empty[String]).toMap
  private type forbiddenMethodsType = Map[ClassFile, mutable.Set[Method]]
  private var forbiddenMethodsMap: forbiddenMethodsType = Map()
  def main(fbMethodsSrcFile: String, jarFileSrc: String): Unit = {
    //Lese datei aus
    val lines: List[String] = Source.fromFile(fbMethodsSrcFile).getLines().toList
    lines.foreach(l => l.replace(" ", ""))

    //Erstelle Set mit verbotenen Methoden
    lines.foreach { line =>
      forbiddenMethods += (line.split(",")(0).replace("class:", "").replace(" ", "") -> mutable.Set.empty)
    }

    //Erstelle Map mit Strings
    val forbiddenMapStr: Map[String, Set[String]] = lines.flatMap { line =>
      val methods = line.split("methods:")(1).replace("{", "").replace("}", "").replace(" ", "").split(",").toSet
      val clsName = line.split(",")(0).replace("class:", "").trim
      Some(clsName -> methods)
    }.toMap


    //Erstelle Projekt:
    val projectJar = new File(jarFileSrc)
    val project = Project(projectJar)

    //Erstelle Map mit richtigen Datentypen

    project.allClassFiles.foreach { classFile =>
      val methodSet = mutable.Set[Method]() // Leeres Set, oder initial befüllt
      classFile.methods.foreach { method =>
        if (forbiddenMapStr(classFile.thisType.simpleName).contains(method.name)) {
          methodSet += method
        }
      }
      forbiddenMethodsMap += (classFile -> methodSet)
    }
    println(forbiddenMethodsMap)
    forbiddenMethodsMap.foreach { classFile =>
      val newClassFileA = modifyCode(forbiddenMethodsMap(classFile._1).toArray)
      val newClassFileABytes: Array[Byte] = Assembler(toDA(newClassFileA))
      Files.write(Paths.get(s"${classFile._1.thisType.simpleName}_copy.class"), newClassFileABytes)
    }
  }

  private def modifyCode(m: Array[Method]): ClassFile ={
    val cf = m(0).classFile

    val newMethods = cf.methods.map{ oldMethod =>
      val oldCode = oldMethod.body.get
      val newInstructions = buildNewInstructionArrayFrom(cf, oldMethod.body.get.instructions)
      oldMethod.copy(body = Some(oldCode.copy(instructions = newInstructions, exceptionHandlers = NoExceptionHandlers)))
      }
    cf.copy(methods = newMethods)
  }

  private def buildNewInstructionArrayFrom(classFile: ClassFile, oldInstructions: Array[Instruction]): Array[Instruction] = {
    oldInstructions
      .filter(_ != null)
      .filter {
        case instr if instr.isInvocationInstruction =>
          // nur behalten, wenn nicht in forbiddenMethodsMap
          !forbiddenMethodsMap(classFile)
            .exists(fm => fm.name == instr.asMethodInvocationInstruction.name)
        case _ =>
          // alle anderen Instruktionen immer behalten
          true
      }
  }
  println("------------------------------------------------\n")
}