package Woche4

object ForbiddenMethods {
  def main(args: Array[String]): Unit = {
    allowedOne()
    fbOne()
    fbTwo("")
    allowedTwo()
    fbThree(3)
    allowedThree()
  }
  private def allowedOne(): Unit = {
    println("Do something allowed")
  }
  private def fbOne(): Unit = {
    println("Do something forbidden")
  }
  private def fbTwo(arg: String): Unit = {
    println("Do something forbidden")
  }
  private def allowedTwo(): Unit = {
    fbOne()
    println("Do something allowed")
  }
  private def fbThree(arg: Int): Unit = {
    println("Do something forbidden")
  }
  private def allowedThree(): Unit = {
    println("Do something allowed")
    allowedOne()
    fbThree(42)
  }
}