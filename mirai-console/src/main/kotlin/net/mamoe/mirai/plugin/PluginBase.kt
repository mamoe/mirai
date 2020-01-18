package net.mamoe.mirai.plugin

import net.mamoe.mirai.utils.DefaultLogger
import java.io.File
import java.util.jar.JarFile


abstract class PluginBase{

    open fun onLoad(){

    }

    open fun onEnable(){

    }

    open fun onDisable(){

    }

    fun getPluginManager():PluginManager{
        return PluginManager
    }


}

class PluginDescription(
    val pluginName: String,
    val pluginAuthor: String,
    val pluginBasePath: String,
    val pluginVersion: String,
    val pluginInfo: String,
    val depends: List<String>,//插件的依赖
    //internal
    var loaded: Boolean = false,
    var noCircularDepend: Boolean = true
) {

    companion object {
        fun readFromContent(content_: String): PluginDescription {
            val content = content_.split("\n")

            var name = "Plugin"
            var author = "Unknown"
            var basePath = "net.mamoe.mirai.PluginMain"
            var info = "Unknown"
            var version = "1.0.0"
            val depends = mutableListOf<String>();

            content.forEach{
                val line = it.trim()
                val lowercaseLine = line.toLowerCase()
                if(it.contains(":")) {
                    when{
                        lowercaseLine.startsWith("name") -> {
                            name = line.substringAfter(":").trim()
                        }
                        lowercaseLine.startsWith("author") -> {
                            author = line.substringAfter(":").trim()
                        }
                        lowercaseLine.startsWith("info") || lowercaseLine.startsWith("information") -> {
                            info = line.substringAfter(":").trim()
                        }
                        lowercaseLine.startsWith("path") || lowercaseLine.startsWith("basepath") -> {
                            basePath = line.substringAfter(":").trim()
                        }
                        lowercaseLine.startsWith("version") || lowercaseLine.startsWith("ver") -> {
                            version = line.substringAfter(":").trim()
                        }
                    }
                }else if(line.startsWith("-")){
                    depends.add(line.substringAfter("-").trim())
                }
            }
            return PluginDescription(name,author,basePath,version,info,depends)
        }
    }
}


object PluginManager{
    private val pluginsPath = System.getProperty("user.dir") + "/plugins/".replace("//","/").also {
        File(it).mkdirs()
    }

    private val logger = DefaultLogger("Plugin Manager")

    //已完成加载的
    private val nameToDescriptionMap: Map<String, PluginDescription> = mutableMapOf()
    private val pluginBaseToDescriptionMap: Map<PluginBase, PluginDescription> = mutableMapOf()


    fun getPluginDataFolder(plugin: PluginBase):File{
        val name = (pluginBaseToDescriptionMap[plugin] ?: error("Plugin not loaded")).pluginName
        return File(pluginsPath + name).also {
            it.mkdirs()
        }
    }


    /**
     * 尝试加载全部插件
     */
    fun loadPlugins(){
        val pluginsFound: MutableMap<String, PluginDescription> = mutableMapOf()
        val pluginsLocation: MutableMap<String, JarFile> = mutableMapOf()

        File(pluginsPath).listFiles()?.forEach { file ->
            if (file != null) {
                if (file.extension == "jar") {
                    val jar = JarFile(file)
                    val pluginYml = jar.entries().asIterator().asSequence().filter { it.name.toLowerCase().contains("resource/plugin.yml") }.firstOrNull()
                    if (pluginYml == null) {
                        logger.info("plugin.yml not found in jar " + jar.name + ", it will not be consider as a Plugin")
                    } else {
                        val description = PluginDescription.readFromContent(pluginYml.extra.toString())
                        pluginsFound[description.pluginName] = description
                        pluginsLocation[description.pluginName] = jar
                    }
                }
            }
        }

        fun checkNoCircularDepends (target:PluginDescription, needDepends: List<String>,existDepends: MutableList<String>) {

            if (!target.noCircularDepend) {
                return
            }

            existDepends.add(target.pluginName)

            if (needDepends.any { existDepends.contains(it) }) {
                target.noCircularDepend = false
            }

            existDepends.addAll(needDepends)

            needDepends.forEach {
                if (pluginsFound.containsKey(it)) {
                    checkNoCircularDepends(pluginsFound[it]!!, pluginsFound[it]!!.depends, existDepends)
                }
            }
        }


        pluginsFound.values.forEach {
            checkNoCircularDepends(it,it.depends, mutableListOf())
        }

        //load


        fun loadPlugin(description: PluginDescription):Boolean{
            if(!description.noCircularDepend){
                return false.also {
                    logger.error("Failed to load plugin " + description.pluginName + " because it has circular dependency")
                }
            }

            //load depends first
            description.depends.forEach{
                if(!pluginsFound.containsKey(it)){
                    return false.also { _ ->
                        logger.error("Failed to load plugin " + description.pluginName + " because it need " + it + " as dependency")
                    }
                }
                val depend = pluginsFound[it]!!
                //还没有加载
                if(!depend.loaded) {
                    if (!loadPlugin(pluginsFound[it]!!)) {
                        return false.also { _ ->
                            logger.error("Failed to load plugin " + description.pluginName + " because " + it + " as dependency failed to load")
                        }
                    }
                }
            }
            //在这里所有的depends都已经加载了


            //loadPlugin

        }


        pluginsFound.values.forEach{ loadPlugin(it) }
    }




}



