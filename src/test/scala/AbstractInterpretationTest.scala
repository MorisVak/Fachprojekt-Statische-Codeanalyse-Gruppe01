import org.opalj.ai.BaseAI
import org.opalj.ai.common.DomainRegistry
import org.opalj.ai.common.DomainRegistry.domainDescriptions
import org.opalj.br.analyses.Project
import org.scalatest.funsuite.AnyFunSuite
import play.api.libs.json.JsArray
import Woche5.AbstractInterpretation
import Woche5.AbstractInterpretation.runAnalysis

import java.io.File

class AbstractInterpretationTest extends AnyFunSuite {


  test("Report has been created") {
    val testDomain = 0
    val testJars = List("pdfbox-2.0.24.jar")
    val reportJson = runAnalysis(testDomain, testJars)

    assert((reportJson \ "filesAnalyzed").as[List[String]].nonEmpty)
    assert((reportJson \ "domainUsed").as[String] == testDomain.toString)
    assert((reportJson \ "methodsFound").as[JsArray].value.nonEmpty) // je nachdem ob Sample.jar totcode hat
    assert((reportJson \ "totalRuntimeMs").as[Long] >= 0)
  }

  test("With invalid Domain1") {
    val testDomain = -1
    val testJars = List("pdfbox-2.0.24.jar")

    intercept[IllegalArgumentException](runAnalysis(testDomain, testJars))
  }
  test("With invalid Domain2") {
    val testDomain = 1000000000
    val testJars = List("pdfbox-2.0.24.jar")

    intercept[IllegalArgumentException](runAnalysis(testDomain, testJars))
  }

}
