package Woche7

import java.io.{File, FileOutputStream}
import scala.collection.mutable
import scala.io.Source

object AktionenAuswahl {
  def main(args: Array[String]): Unit = {
    val rules = Source.fromFile("src/main/scala/Woche7/Actions.txt").getLines().toArray
    val paramsTxt = Source.fromFile("src/main/scala/Woche7/Params.txt").getLines().toArray
    val resultFolder = rules(rules.length-1).split(":")(1)
    rules.dropRight(1)
    val params = mutable.Map.empty[String, Array[String]]
    println(paramsTxt(5))
    paramsTxt.foreach {param =>
      val parts = param.split(";", 2)
      if (parts.length == 2) {
        val key = parts(0).trim
        val values: Array[String] = parts(1).split(",").map { v =>
          if (v.contains(":")) {
            v.split(":")(1).trim
          } else {
            v
          }
        }
        params += (key -> values)
      }
    }

    //TODO: Die eigentlichen Prints müssen in einer LOG Datei gespeichert werden

    //TODO: Alle System.exit() müssen ersetzt werden

    //TODO: Alle Debugging println müssen entfernt werden

    /*TODO: Gerade wird bei jedem Durchlauf irgendwie (zumindest bei mir) ein gleichnahmiger Ordner erstellt
      mit dem gleichen Namen was dann zu einem Namenskonflikt führt
    */
    val outputDir = new File(resultFolder)
    if (!outputDir.exists()) {
      val ok = outputDir.mkdirs()
      if (!ok)
        throw new RuntimeException(s"Konnte Verzeichnis nicht erstellen: $resultFolder")
    }

    //Welche Aktionen (also Wochen) ausgeführt werden sollen steht in der Actions.txt
    //Die benötigten Parameter der einzelnen Methoden stehen in der Params.txt


    rules.foreach { rule =>
      if (rule.split(":")(1).trim == "true"){
        rule.split(":")(0) match {
          case "godclass" =>
            val outFile = new java.io.File(resultFolder, "godClassOut.txt")
            val fos = new FileOutputStream(outFile)
            Console.withOut(fos) {
              godclass.main(params("godclass")(0).toInt, params("godclass")(1).toInt, params("godclass")(2).toInt, params("godclass")(3))
            }
            fos.close()
          case "criticalMethods" =>
            val outFile = new java.io.File(resultFolder, "criticalMethodsOut.txt")
            val fos = new FileOutputStream(outFile)
            Console.withOut(fos) {
              criticalMethods.main(params("criticalMethods")(0), params("criticalMethods")(1), params("criticalMethods")(2))
            }
            fos.close()
          case "ThirdPartyLibraries" =>
            val outFile = new java.io.File(resultFolder, "TPLOut.txt")
            val fos = new FileOutputStream(outFile)
            Console.withOut(fos) {
              Analyze_TPL.main(params("ThirdPartyLibraries")(0), params("ThirdPartyLibraries")(1), params("ThirdPartyLibraries")(2))
            }
            fos.close()
          case "ForbiddenMethods" =>
            val outFile = new java.io.File(resultFolder, "ForbiddenMethodsOut.txt")
            val fos = new FileOutputStream(outFile)
            Console.withOut(fos) {
              bytecode_mod.main(params("ForbiddenMethods")(0), params("ForbiddenMethods")(1))
            }
            fos.close()
          case "DeadCodeAnalysis" =>
            val outFile = new java.io.File(resultFolder, "DeadCodeAnalysisOut.txt")
            val fos = new FileOutputStream(outFile)
            Console.withOut(fos) {
              AnalyzeDeadCode.main()
            }
            fos.close()
          case "ArchitectureValidation" =>
            val outFile = new java.io.File(resultFolder, "ArchitectureValidationOut.txt")
            val fos = new FileOutputStream(outFile)
            Console.withOut(fos) {
              ArchitectureValidation.main(params("ArchitectureValidation")(0),params("ArchitectureValidation")(1))
            }
            fos.close()
          case _ =>
            println("Ungültiger Eintrag in der Config Datei")
            System.exit(0)
        }
      }
    }
  }
}
