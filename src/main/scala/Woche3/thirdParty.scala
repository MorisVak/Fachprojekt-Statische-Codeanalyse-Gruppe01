import com.typesafe.config.{Config, ConfigValueFactory}

import scala.io.Source
import org.opalj.BaseConfig
import scala.collection.mutable
import org.opalj.br.analyses.Project
import org.opalj.log.GlobalLogContext
import org.opalj.tac.cg.{CFA_1_1_CallGraphKey, CHACallGraphKey, CallGraphKey, RTACallGraphKey, XTACallGraphKey}


object thirdParty{
  def main(args: Array[String]):Unit = {
    //ALL CALLGRAPHS
    val callGraphStrategies: Map[String, CallGraphKey] = Map(
      "RTA" -> RTACallGraphKey,
      "CHA" -> CHACallGraphKey,
      "XTA" -> XTACallGraphKey,
      "CFA" -> CFA_1_1_CallGraphKey
    )

    //powermock-reflect-2.0.9 -> Anzahl an Methoden
    //SETS TO SAFE DATA
    val setOfusedMethodsHibernate = mutable.Set.empty[String]
    val setOfpublicApiMethodsHibernate = mutable.Set.empty[(String, String,String)]
    val setOfContainedMethodsHibernate = mutable.Set.empty[(String,String,String)]

    val setOfusedMethodsSpring = mutable.Set.empty[String]
    val setOfpublicApiMethodsSpring = mutable.Set.empty[(String, String,String)]
    val setOfContainedMethodsSpring = mutable.Set.empty[(String,String,String)]

    val setOfusedMethodsPowerMock = mutable.Set.empty[String]
    val setOfpublicApiMethodsPowerMock = mutable.Set.empty[(String, String,String)]
    val setOfContainedMethodsPowerMock = mutable.Set.empty[(String,String,String)]

    //LOADING JARS
    val jarFile = new java.io.File("openmrs-api-2.8.0-SNAPSHOT.jar")
    val hibernateFile = new java.io.File ("hibernate-core-5.6.15.Final.jar")
    val springFile = new java.io.File ("spring-core-5.3.30.jar")
    val powerMockFile = new java.io.File ("powermock-reflect-2.0.9.jar")

    val jarFiles = List(
      new java.io.File("openmrs-api-2.8.0-SNAPSHOT.jar"),
      new java.io.File ("hibernate-core-5.6.15.Final.jar"),
      new java.io.File ("spring-core-5.3.30.jar")
    )

    //GETTING THE INPUT
    var cgStrategy = ""
    val source3 = Source.fromFile("src/main/scala/Woche3/call-graph.txt")
    try
      cgStrategy = source3.getLines().mkString
    finally
      source3.close()

    implicit val config: Config =
      BaseConfig.withValue(
        "org.opalj.br.analyses.cg.InitialEntryPointsKey.analysis",
        ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.LibraryEntryPointsFinder")
      ).withValue(
        "org.opalj.br.analyses.cg.InitialInstantiatedTypesKey.analysis",
        ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.LibraryInstantiatedTypesFinder")
      )

    //val allSetsOfMethods: Map[String, Set[String]] = libraryJarNames.map(k => k -> Set.empty[String]).toMap
    /*
    *
    * hibernate-core-5.6.15.Final

      spring-core-5.3.30

      powermock-reflect-2.0.9
    *
    *
    * spring
    * */


    //LOADING IN PROJECTS
    val project = Project(jarFile, GlobalLogContext, config)
    val hibernateProject = Project (hibernateFile)
    val springProject = Project (springFile)
    val powerMockProject = Project (powerMockFile)

    //GETTING HIBERNATE METHODS
    val classFilesHibernate = hibernateProject.allClassFiles
    println(project.classFilesCount)
    classFilesHibernate.foreach{ classFile =>
      val fqn = classFile.fqn
      val splitFqn = fqn.split("/").mkString(".")
      val methods = classFile.methods
      methods.foreach{ method =>
        println(method.fullyQualifiedSignature)
        setOfContainedMethodsHibernate += ((method.fullyQualifiedSignature,method.name, splitFqn))
        if(method.isPublic && !method.isSynthetic && !classFile.thisType.packageName.contains("internal")){
          setOfpublicApiMethodsHibernate += ((method.fullyQualifiedSignature, method.name, splitFqn))
        }
      }
    }

    //GETTING SPRING METHODS
    val classFilesSpring = springProject.allClassFiles
    println(project.classFilesCount)
    classFilesSpring.foreach{ classFile =>
      val fqn = classFile.fqn
      val splitFqn = fqn.split("/").mkString(".")
      val methods = classFile.methods
      methods.foreach{ method =>
        println(method.fullyQualifiedSignature)
        setOfContainedMethodsSpring += ((method.fullyQualifiedSignature,method.name, splitFqn))
        if(method.isPublic && !method.isSynthetic && !classFile.thisType.packageName.contains("internal")){
          setOfpublicApiMethodsSpring += ((method.fullyQualifiedSignature, method.name, splitFqn))
        }
      }
    }

    //GETTING POWERMOCK METHODS
    val classFilesPowerMock = powerMockProject.allClassFiles
    println(project.classFilesCount)
    classFilesPowerMock.foreach{ classFile =>
      val fqn = classFile.fqn
      val splitFqn = fqn.split("/").mkString(".")
      val methods = classFile.methods
      methods.foreach{ method =>
        println(method.fullyQualifiedSignature)
        setOfContainedMethodsPowerMock += ((method.fullyQualifiedSignature,method.name, splitFqn))
        if(method.isPublic && !method.isSynthetic && !classFile.thisType.packageName.contains("internal")){
          setOfpublicApiMethodsPowerMock += ((method.fullyQualifiedSignature, method.name, splitFqn))
        }
      }
    }

    //BUILDING CALLGRAPH OF THE WHOLE PROJECT
    val callGraph = callGraphStrategies(cgStrategy)
    val cg = project.get(callGraph)
    cg.reachableMethods.foreach(c => {
      cg.calleesOf(c.method).foreach(p => {
        p._2.foreach(u => {
          //CHECKING IF HIBERNATE METHOD
          if(u.method.toString.contains("org/spring")) {
            setOfContainedMethodsHibernate.foreach((entry) => {
              if (entry._2 == u.method.name && u.method.toString().contains(entry._3)){

                //println(s"USED --- ${u.method} \n Name of the Method :  ${u.method.name} \n ")
                //println(s"FQN : ${entry._1} NAME : ${entry._2} CLASS : ${entry._3} \n")
                setOfusedMethodsHibernate += (u.method.toString)
              }
            })
            //println(s"--- ${u.toString} \n Name of the Method :  ${u.method.name} \n ")
          }
          //CHECKING IF SPRING METHOD
          if(u.method.toString.contains("spring")) {
            setOfContainedMethodsSpring.foreach((entry) => {
              if (entry._2 == u.method.name && u.method.toString().contains(entry._3)){

                //println(s"USED --- ${u.method} \n Name of the Method :  ${u.method.name} \n ")
                //println(s"FQN : ${entry._1} NAME : ${entry._2} CLASS : ${entry._3} \n")
                setOfusedMethodsSpring += (u.method.toString)
              }
            })
            //println(s"--- ${u.toString} \n Name of the Method :  ${u.method.name} \n ")
          }
          //CHECKING IF POWERMOCK METHOD
          if(u.method.toString.contains("org.powermock")) {
            setOfContainedMethodsPowerMock.foreach((entry) => {
              if (entry._2 == u.method.name && u.method.toString().contains(entry._3)){

                //println(s"USED --- ${u.method} \n Name of the Method :  ${u.method.name} \n ")
                //println(s"FQN : ${entry._1} NAME : ${entry._2} CLASS : ${entry._3} \n")
                setOfusedMethodsPowerMock += (u.method.toString)
              }
            })
            //println(s"--- ${u.toString} \n Name of the Method :  ${u.method.name} \n ")
          }
          })
        })
      })

    setOfContainedMethodsPowerMock.foreach((entry) => {
      println(s"USED --- ${entry._1} \n Name of the Method :  ${entry._2} \n ")
    })

    println(s"Projects Method total : ${project.methodsCount}")
    println(s"Used ${cgStrategy} callgraph \n --------------------------------------------------\n")
    println("---------------HIBERNATE---------------")
    println(s"Total methods used from hibernate = ${setOfusedMethodsHibernate.size} of ${hibernateProject.methodsCount}")
    println(s"Total methods used public api hibernate = ${setOfusedMethodsHibernate.size} of ${setOfpublicApiMethodsHibernate.size} \n")
    println("---------------SPRING---------------")
    println(s"Total methods used from Spring = ${setOfusedMethodsSpring.size} of ${springProject.methodsCount}")
    println(s"Total methods used public api Spring = ${setOfusedMethodsSpring.size} of ${setOfpublicApiMethodsSpring.size} \n")
    println("---------------POWERMOCK---------------")
    println(s"Total methods used from PowerMock = ${setOfusedMethodsPowerMock.size} of ${powerMockProject.methodsCount}")
    println(s"Total methods used public api PowerMock = ${setOfusedMethodsPowerMock.size} of ${setOfpublicApiMethodsPowerMock.size}")
  }
}