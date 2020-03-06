package net.mamoe.mirai.console.wrapper

import java.io.File

object LibManager{

    val libPath by lazy{
        File(contentPath.absolutePath + "/lib/").also {
            if(!it.exists()){
                it.mkdirs()
            }
        }
    }

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
     * 由Pom content提供必要依赖
     * LibManager会检查所有dependency的dependency
     */
    internal fun addDependencyByPom(pomContent:String){

    }


    /**
     * 由Pom Path提供必要依赖
     * LibManager会进行下载和递归处理
     */
    fun addDependencyRequest(link:String){
        val stream = kotlin.runCatching {
               val jcenterPath =  "https://jcenter.bintray.com/{group}/{project}/{version}/:{project}-{version}.pom"
               val aliyunPath =  "https://maven.aliyun.com/nexus/content/repositories/jcenter/{group}/{project}/{version}/{project}-{version}.pom"
        }
    }

    /**
     * 普通的增加一个dependency
     */
    fun addDependency(){

    }

    suspend fun downloadIfNeeded(){

    }

}