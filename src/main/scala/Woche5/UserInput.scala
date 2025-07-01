package Woche5

import scala.swing._
import scala.swing.event._
import scala.collection.mutable.ArrayBuffer
import scala.io.Source // Import for reading files

// Eine Case-Klasse, um die eingegebenen Daten sauber zu transportieren
case class UserInput(someNumber: Int, filePaths: List[String])

class InputWindow extends Dialog {

  // --- 1. Fenstereigenschaften definieren ---
  title = "Analyse Konfiguration"
  resizable = false
  modal = true // Stellt sicher, dass der Dialog den Programmfluss blockiert

  // --- 2. UI-Komponenten ---
  private val numberLabel = new Label("Domain-Index (z.B. 0):")
  private val numberField = new TextField { columns = 10 }
  private val filesLabel = new Label("Zu analysierende Dateien:")
  private val fileListBuffer = new ArrayBuffer[String]()
  private val fileListView = new ListView(fileListBuffer) {
    preferredSize = new Dimension(350, 150)
  }
  private val fileListScrollPane = new ScrollPane(fileListView)
  private val newFileField = new TextField { columns = 25 }
  private val addButton = new Button("Hinzufügen")
  private val removeButton = new Button("Entfernen")
  private val loadButton = new Button("Load") // NEU: Der Load-Button
  private val okButton = new Button("Analyse starten")
  private val cancelButton = new Button("Abbrechen")

  // --- 3. Layout (Optimierungen hier) ---
  contents = new BoxPanel(Orientation.Vertical) {
    border = Swing.EmptyBorder(10, 10, 10, 10) // Add some padding around the entire dialog

    // Top section: Number input
    contents += new FlowPanel(FlowPanel.Alignment.Left)() {
      contents += numberLabel
      contents += Swing.HStrut(5) // Small horizontal space
      contents += numberField
    }

    contents += Swing.VStrut(10) // Vertical space between sections

    // Middle section: File list and label
    contents += new BoxPanel(Orientation.Vertical) {
      contents += new FlowPanel(FlowPanel.Alignment.Left)(filesLabel) // Label above the list
      contents += Swing.VStrut(5)
      contents += fileListScrollPane
    }

    contents += Swing.VStrut(10)

    // File addition/removal/load section
    contents += new FlowPanel(FlowPanel.Alignment.Left)() {
      contents += newFileField
      contents += Swing.HStrut(5)
      contents += addButton
      contents += Swing.HStrut(5)
      contents += removeButton
      contents += Swing.HStrut(5) // NEU: Abstand zum Load-Button
      contents += loadButton // NEU: Load-Button hinzugefügt
    }

    contents += Swing.VStrut(15) // More vertical space before action buttons

    // Bottom section: Action buttons
    contents += new FlowPanel(FlowPanel.Alignment.Right)() {
      contents += okButton
      contents += Swing.HStrut(10) // Space between buttons
      contents += cancelButton
    }
  }

  centerOnScreen()

  // --- 4. Event-Handling ---
  private var result: Option[UserInput] = None
  // NEU: loadButton zu den Listeners hinzufügen
  listenTo(addButton, removeButton, loadButton, okButton, cancelButton, newFileField.keys)

  reactions += {
    case ButtonClicked(`addButton`) | KeyPressed(_, Key.Enter, _, _) if newFileField.text.trim.nonEmpty =>
      fileListBuffer += newFileField.text.trim
      fileListView.listData = fileListBuffer
      newFileField.text = ""
      newFileField.requestFocusInWindow()

    case ButtonClicked(`removeButton`) =>
      if (fileListView.selection.indices.nonEmpty) {
        for (index <- fileListView.selection.indices.toList.sorted.reverse) {
          fileListBuffer.remove(index)
        }
        fileListView.listData = fileListBuffer
      }

    // NEU: Reaktion für den Load-Button
    case ButtonClicked(`loadButton`) =>
      val filePath = "JarFiles.txt" // Der Name der Datei
      try {
        val lines = Source.fromFile(filePath).getLines()
        var filesAddedCount = 0
        lines.foreach { line =>
          val trimmedLine = line.trim
          if (trimmedLine.nonEmpty && !fileListBuffer.contains(trimmedLine)) { // Avoid adding duplicates
            fileListBuffer += trimmedLine
            filesAddedCount += 1
          }
        }
        if (filesAddedCount > 0) {
          fileListView.listData = fileListBuffer // Update the list view
          Dialog.showMessage(this, s"$filesAddedCount Dateien aus '$filePath' geladen.", "Information", Dialog.Message.Info)
        } else {
          Dialog.showMessage(this, "Keine neuen Dateien gefunden oder alle Einträge existieren bereits in der Liste.", "Information", Dialog.Message.Info)
        }
      } catch {
        case e: java.io.FileNotFoundException =>
          Dialog.showMessage(this, s"Die Datei '$filePath' wurde nicht gefunden.", "Fehler", Dialog.Message.Error)
        case e: Exception =>
          Dialog.showMessage(this, s"Fehler beim Lesen der Datei '$filePath': ${e.getMessage}", "Fehler", Dialog.Message.Error)
      }


    case ButtonClicked(`okButton`) =>
      val numberOpt = numberField.text.toIntOption
      numberOpt match {
        case Some(num) if fileListBuffer.nonEmpty =>
          result = Some(UserInput(num, fileListBuffer.toList))
          close()
        case Some(_) =>
          Dialog.showMessage(this, "Bitte fügen Sie mindestens eine Datei hinzu.", "Fehler", Dialog.Message.Error)
        case None =>
          Dialog.showMessage(this, "Bitte geben Sie eine gültige Zahl für den Index ein.", "Fehler", Dialog.Message.Error)
      }

    case ButtonClicked(`cancelButton`) =>
      result = None
      close()
  }

  /**
   * Zeigt den Dialog an und gibt das Ergebnis zurück, nachdem er geschlossen wurde.
   */
  def showAndGetInput(): Option[UserInput] = {
    // Die open-Methode eines modalen Dialogs blockiert, bis close() aufgerufen wird.
    open()
    result
  }
}