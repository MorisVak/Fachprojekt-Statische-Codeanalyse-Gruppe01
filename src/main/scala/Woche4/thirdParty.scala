package Woche4

import com.typesafe.config.{Config, ConfigValueFactory}
import org.opalj.BaseConfig
import org.opalj.br.analyses.Project
import org.opalj.log.GlobalLogContext
import org.opalj.tac.cg._
import org.opalj.ba._
import org.opalj.br._
import org.opalj.br.instructions._
import java.nio.file.{Files, Paths}
import scala.collection.immutable.ArraySeq
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
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

    val setOfusedMethodsHibernate = mutable.Set.empty[(String,String)]

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
              setOfusedMethodsHibernate += ((u.method.declaringClassType.fqn, u.method.name))
            }
          } )
        })
      })
    })

    setOfusedMethodsHibernate.foreach(string => {
      println(string)
    })
    //println(s"FINAL AMOUNT = ${setOfusedMethodsHibernate.size}")


    val hibernateClassFiles = hibernateProject.allClassFiles
    hibernateClassFiles.foreach{ file =>
      var methodsToUse = ArrayBuffer.empty[METHOD[(Map[org.opalj.br.PC,Nothing], List[String])]]
      file.methods.foreach( method => {
        setOfusedMethodsHibernate.foreach( usedMethod => {
          //println(s" Method used :${file.fqn} : ${method.classFile.fqn} --- ${usedMethod._1} || ${{usedMethod._2}}")
          if( usedMethod._1 == method.classFile.fqn && usedMethod._2 == method.name){
            println(method.returnType.toString)
            if(method.isPublic){
              if(!method.returnType.isVoidType) {
                methodsToUse += METHOD(PUBLIC, s"${method.name}", s"${method.descriptor}", CODE(
                  ACONST_NULL,
                  ARETURN
                ))
              }else{
                methodsToUse += METHOD(PUBLIC, s"${method.name}", s"${method.descriptor}", CODE(
                  RETURN
                ))
              }
            }else{
              println("FOUND A PRIVATE METHOD")
              if(!method.returnType.isVoidType) {
                methodsToUse += METHOD(PRIVATE, s"${method.name}", s"${method.descriptor}", CODE(
                  ACONST_NULL,
                  ARETURN
                ))
              }else{
                methodsToUse += METHOD(PRIVATE, s"${method.name}", s"${method.descriptor}", CODE(
                  RETURN
                ))
              }
            }
          }
        })
      })

      println( s"!!!!!!!!!!!!!!!!!!!!!! ${file.fqn}")
      val cb = CLASS (thisType = s"${file.fqn}",
      methods = METHODS (ArraySeq.from(methodsToUse)))
      val (daClassFile, codeAnnotations) = cb.toDA()
      val rawClassFile : Array[Byte] = org.opalj.bc.Assembler(daClassFile)
      java.nio.file.Files.write(Paths.get(s"src/main/scala/Woche4/classFiles",s"dummy-${file.thisType.packageName.split("/")(file.thisType.packageName.split("/").length-1)}.class"),rawClassFile)
      methodsToUse = ArrayBuffer.empty[METHOD[(Map[org.opalj.br.PC,Nothing], List[String])]]
    }
    println(s"FINAL AMOUNT = ${setOfusedMethodsHibernate.size}")
  }

}