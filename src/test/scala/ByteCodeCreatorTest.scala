import Woche4.byteCodeCreator
import Woche4.byteCodeCreator.{analyzeCallGraph, defaultConfig, generateDummyClasses, loadProject, loadTPLProject}
import com.typesafe.config.Config
import org.scalatest.funsuite.AnyFunSuite
import org.opalj.bc._
import org.opalj.br._
import org.opalj.ba.{CLASS, CODE, METHOD, METHODS, PUBLIC}
import org.opalj.br.analyses.Project
import org.opalj.br.instructions.RETURN
import org.opalj.br.reader.Java8Framework.ClassFiles
import org.opalj.util.InMemoryClassLoader

import java.io.{ByteArrayInputStream, File}
import java.net.URL
import java.nio.file.{Files, Paths}
import scala.collection.IterableOnce.iterableOnceExtensionMethods

class ByteCodeCreatorTest extends AnyFunSuite {

  implicit val config: Config = defaultConfig

  val testProject : Project[URL] = loadProject("openmrs-api-2.8.0-SNAPSHOT.jar", config)
  val testTplProject : Project[URL] = loadTPLProject("hibernate-core-5.6.15.Final.jar")

  val usedTestMethods: Set[(ObjectType, String, MethodDescriptor)] = analyzeCallGraph(testProject, testTplProject)

  var usedConstructorCount : Int = usedTestMethods.count(_._2 == "<init>")

  generateDummyClasses(usedTestMethods)

  var classCount : Int = usedTestMethods.map(_._1.fqn).size

  test("Count of TPL-Classes Assertion") {
    assert(countClassFilesInDirectory("src/main/scala/Woche4/dummies") == classCount)
  }

  val dummyPath = "src/main/scala/Woche4/dummies"
  val totalMethods : Int = countMethodsInDirectory(dummyPath, usedConstructorCount, classCount)

  test("Count of TPL Methods") {
    assert(totalMethods == usedTestMethods.size)
  }

  test("All Methods Used") {
    assert(compareActualToCreated(usedTestMethods, dummyPath))
  }

  def countClassFilesInDirectory(path: String): Int = {
    val dir = new File(path)
    if (dir.exists && dir.isDirectory) {
      dir.listFiles((file: File) => file.isFile && file.getName.endsWith(".class")).length
    } else {
      0
    }
  }

  def countMethodsInDirectory(path: String, usedConstructors: Int, classCount: Int): Int = {
    val dummyProject = Project(new File(path))
    (dummyProject.allClassFiles.map(_.methods.size).sum) + usedConstructors - classCount
  }

  def compareActualToCreated(actualMethods: Set[(ObjectType, String, MethodDescriptor)], path: String): Boolean = {

    val dummyProject = Project(new File(path))

    val dummyMethods: Set[(ObjectType, String, MethodDescriptor)] =
      dummyProject.allClassFiles.flatMap { cf =>
        val objType: ObjectType = cf.thisType
        cf.methods.map { m =>
          (objType, m.name, m.descriptor)
        }
      }.toSet

    val missing = actualMethods.diff(dummyMethods)
    println(missing.size)

    missing.isEmpty

  }

}

