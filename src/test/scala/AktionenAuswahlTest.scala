import org.scalatest.funsuite.AnyFunSuite

import java.io.File

class AktionenAuswahlTest extends AnyFunSuite {
  test("Result directory and results created") {
    Woche7.AktionenAuswahl
    val f1 = new File(" Results/architechtureValidation_result.txt")
    assert(f1.exists())
  }
}
