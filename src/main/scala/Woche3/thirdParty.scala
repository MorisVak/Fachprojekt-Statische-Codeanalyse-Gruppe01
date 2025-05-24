import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import scala.io.Source
import org.opalj.BaseConfig
import org.opalj.ai.domain.l2.DefaultPerformInvocationsDomainWithCFGAndDefUse
import org.opalj.ai.fpcf.properties.AIDomainFactoryKey
import scala.collection.mutable
import org.opalj.br.DeclaredMethod
import org.opalj.br.analyses.Project
import org.opalj.br.fpcf.properties.Context
import org.opalj.br.instructions.{INVOKESTATIC, INVOKEVIRTUAL, MethodInvocationInstruction}
import org.opalj.log.GlobalLogContext
import org.opalj.tac.cg.RTACallGraphKey

object thirdParty{
  def main(args: Array[String]):Unit = {

  }
}