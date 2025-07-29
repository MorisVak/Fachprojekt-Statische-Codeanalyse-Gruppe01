package Woche7

import java.time.LocalDateTime
import scala.collection.mutable

case class DeadCodeReport(
                         filesAnalyzed: List[String],
                         domainUsed: String,
                         timeFinished: LocalDateTime,
                         totalRuntimeMs: Long,
                         methodsFound: mutable.Set[MethodWithDeadCode]
                         ) {

}

