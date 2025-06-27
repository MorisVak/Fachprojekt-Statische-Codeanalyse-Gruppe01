package Woche5
import java.time.LocalDateTime

case class DeadCodeReport(
                         filesAnalyzed: List[String],
                         domainUsed: String,
                         timeFinished: LocalDateTime,
                         totalRuntimeMs: Long
                         ) {

}

