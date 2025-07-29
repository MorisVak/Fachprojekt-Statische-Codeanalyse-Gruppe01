import org.opalj.ai.BaseAI
import org.opalj.ai.common.DomainRegistry
import org.opalj.ai.common.DomainRegistry.domainDescriptions
import org.opalj.br.analyses.Project
import org.scalatest.funsuite.AnyFunSuite
import play.api.libs.json.{JsArray, JsValue, Json}
import Woche5.AnalyzeDeadCode
import Woche5.AnalyzeDeadCode.runAnalysis

import java.io.File
import scala.collection.mutable

class AbstractInterpretationTest extends AnyFunSuite {


  test("Report has been created") {
    val testDomain = 0
    val testJars = mutable.Set("pdfbox-2.0.24.jar")
    runAnalysis(testDomain, testJars)
  }

  test("With invalid Domain1") {
    val testDomain = -1
    val testJars = mutable.Set("pdfbox-2.0.24.jar")

    intercept[NoSuchElementException](runAnalysis(testDomain, testJars))
  }
  test("With invalid Domain2") {
    val testDomain = 1000000000
    val testJars = mutable.Set("pdfbox-2.0.24.jar")

    intercept[NoSuchElementException](runAnalysis(testDomain, testJars))
  }

}
