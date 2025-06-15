package Woche4

import com.typesafe.config.{Config, ConfigValueFactory}
import org.opalj.BaseConfig
import org.opalj.br.analyses.Project
import org.opalj.log.GlobalLogContext
import org.opalj.tac.cg._
import org.opalj.ba._
import org.opalj.br._
import org.opalj.br.instructions._


import scala.collection.mutable
import scala.io.Source


object thirdParty{
  def main(args: Array[String]):Unit = {
    implicit val config: Config =
      BaseConfig.withValue(
        "org.opalj.br.analyses.cg.InitialEntryPointsKey.analysis",
        ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.LibraryEntryPointsFinder")
      ).withValue(
        "org.opalj.br.analyses.cg.InitialInstantiatedTypesKey.analysis",
        ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.LibraryInstantiatedTypesFinder")
      )


    val project = Project(new java.io.File("openmrs-api-2.8.0-SNAPSHOT.jar"), GlobalLogContext, config)
    val firstProject = Project(new java.io.File("openmrs-api-2.8.0-SNAPSHOT.jar"))
    val hibernateProject = Project(new java.io.File ("hibernate-core-5.6.15.Final.jar"))

    val setOfusedMethodsHibernate = mutable.Set.empty[String]

    hibernateProject.packages

    println(hibernateProject.packages)

    val cg = project.get(RTACallGraphKey)
    cg.reachableMethods.foreach(c => {
      cg.calleesOf(c.method).foreach(p => {
        p._2.foreach(u => {
          //println("--------------------------")
          //println(s" PACKAGE :: ${u.method.declaringClassType.packageName}")
          hibernateProject.packages.foreach(pack => {
            //println(pack)
            if (u.method.declaringClassType.packageName == pack){
              setOfusedMethodsHibernate += u.method.descriptor.toString()
            }
          } )
        })
      })
    })

    setOfusedMethodsHibernate.foreach(string => {
      println(string)
    })
    println(s"FINAL AMOUNT = ${setOfusedMethodsHibernate.size}")


    val hibernateClassFiles = hibernateProject.allClassFiles
    hibernateClassFiles.foreach{ file =>
      file.methods.foreach( method => {
        setOfusedMethodsHibernate.foreach( usedMethod => {
          //println(s" Method used :${file.fqn} ${method.name} --- ${usedMethod}")
          if( usedMethod == method.descriptor.toString){
            println("WE ARE THE SAME")
          }
        })
      })
    }
  }

}