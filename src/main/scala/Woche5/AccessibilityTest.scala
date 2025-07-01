
object AccessibilityTest {

  def main(args: Array[String]): Unit = {
    mOne()
    mTwo()
    mThree()
    mFour()
  }
  def mOne(): Unit = {
    var i=0
    i+=2
    if (i==3){
      println("Das sollte nicht erreichbar sein!")
    }
  }
  def mTwo(): Unit = {
    var i=0
    val seq = Seq(1,2,3,4,5,6,7,8,9)
    for (_ <- seq){
      i+=1
    }
    if (i>=10){
      println("Das sollte nicht erreichbar sein!")
    }
  }
  def mThree(): Unit = {
    //In der Methode ist ALLES erreichbar
    var i=0
    i+=1
    val j=10
    i+=j
    if(i>=11){
      println("Das ist erreichbar!") 
    }
  }
  def mFour(): Unit = {
    var i=0
    if(Math.random() > 0.5) i=2
    if(i==1){
      println("Das sollte nicht erreichbar sein!")
    }
  }
}