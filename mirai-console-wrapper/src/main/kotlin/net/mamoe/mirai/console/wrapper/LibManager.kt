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
    fun addDependencyByPom(pomContent:String){

    }


    /**
     * 普通的增加一个dependency
     */
    fun addDependency(){

    }

    suspend fun downloadIfNeeded(){

    }

}