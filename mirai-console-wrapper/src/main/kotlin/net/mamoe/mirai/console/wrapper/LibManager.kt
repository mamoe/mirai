package net.mamoe.mirai.console.wrapper

import java.io.File
import java.util.*

object LibManager{

    val libPath by lazy{
        File(contentPath.absolutePath + "/lib/").also {
            if(!it.exists()){
                it.mkdirs()
            }
        }
    }

    /**
     * 开关
     * 当前版本不写dynamic加载lib
     */
    val dynamic = false;

    fun clearLibs(){
        libPath.listFiles()?.forEach {
            it.delete()
        }
    }


    /**
     * 增加dependency 不是立刻下载
     * 全部完成后使用 @link downloadIfNeeded()开始下载
     */

    /**
     * 由Pom Path提供必要依赖
     * LibManager会进行下载和递归处理
     */
    suspend fun addDependencyRequest(
        group:  String,
        project: String,
        version: String
    ){
        if(!dynamic){
            return;
        }
        var pom:String? = null
        if(project.contains("maven") && project.contains("plugin")){
            return
        }
        if(project.contains("toplink-essentials") || project.contains("ejb") ||project.contains("glassfish-embedded-all") || project.contains("maven-bundle-plugin") || project.contains("jetty") || project.contains("plexus-component-annotations") || project.contains("slf4j")  || project.contains("sisu-inject-plexus") || project.contains("maven-remote-resources-plugin") || project.contains("easymock") || project.contains("junit") || project.contains("log4j") || project.contains("doxia-logging-api") || project.contains("maven-enforcer-plugin") || project.contains("maven-plugin") || project.contains("maven-artifact") || project.contains("maven-core") || project.contains("cglib") || project.contains("spring-core")){
            return
        }
        tryNTimesOrQuit(3, "Failed to find dependency for $project") {
            pom = Http.downloadMavenPomAsString(
                group, project, version
            )
        }
        addDependency(group,project,version)
        if(pom == null){
            println("Failed to load dependency POM")
            return
        }
       pom!!.replace("\n","").split("</dependency>").forEach {
           if(it.contains("<dependency>")) {
               val dependencyInfo = it.replace("<dependency>","")
               if(dependencyInfo.contains("<groupId>") && dependencyInfo.contains("<artifactId>") && dependencyInfo.contains("<version>")) {
                   val groupName =
                       dependencyInfo.substringAfter("<groupId>").substringBefore("</groupId>").replace(".", "/")
                           .removeSuffix("/")
                   val projectName = dependencyInfo.substringAfter("<artifactId>").substringBefore("</artifactId>")
                   val versionName = dependencyInfo.substringAfter("<version>").substringBefore("</version>")
                   if (!versionName.contains("{")) {
                       if (addDependency(groupName, projectName, versionName)) {
                           addDependencyRequest(groupName, projectName, versionName)
                       }
                   }
               }
           }
        }
    }

    /**
     * 普通的增加一个dependency
     */
    private val dependency = HashSet<String>()
    fun addDependency(
        group:  String,
        project: String,
        version: String
    ):Boolean{
        if(!dynamic){
            return false;
        }
        if(project.contains("maven") && project.contains("plugin")){
            return true
        }
        if(project.contains("toplink-essentials") ||project.contains("ejb") || project.contains("glassfish-embedded-all") || project.contains("maven-bundle-plugin") || project.contains("jetty") || project.contains("slf4j") || project.contains("sisu-inject-plexus") || project.contains("maven-remote-resources-plugin") || project.contains("easymock") || project.contains("junit") || project.contains("log4j") || project.contains("doxia-logging-api") || project.contains("maven-enforcer-plugin") || project.contains("maven-plugin") || project.contains("maven-artifact") || project.contains("maven-core") || project.contains("cglib") || project.contains("spring-core")){
            return true
        }
        val id = "${group
            .replace(".","/")
            .removeSuffix("/")
        }-$project:$version"
        if(dependency.contains(id)){
            return false
        }
        println(id)
        dependency.add(id)
        return true
    }

    suspend fun downloadIfNeeded(){
        this.dependency.forEach {
            println(it)
        }
    }

}