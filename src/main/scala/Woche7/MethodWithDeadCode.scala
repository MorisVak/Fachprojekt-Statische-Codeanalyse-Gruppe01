package Woche7

import scala.collection.mutable


case class MethodWithDeadCode(
                               fullSignature: String,
                               numberOfTotalInstructions: Int,
                               numberOfDeadInstructions: Int,
                               enclosingTypeName: String,
                               deadInstructions: mutable.Set[DeadInstruction]
                             ) {
}