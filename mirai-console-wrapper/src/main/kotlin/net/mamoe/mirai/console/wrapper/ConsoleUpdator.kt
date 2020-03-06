package net.mamoe.mirai.console.wrapper

import io.ktor.client.request.get
import io.ktor.http.URLProtocol
import java.io.File
import kotlin.math.pow
import kotlin.system.exitProcess

const val CONSOLE_PURE = "Pure"

object ConsoleUpdator{

    @Suppress("SpellCheckingInspection")
    private object Links:HashMap<String,Map<String,String>>() {
       init {
           put(CONSOLE_PURE, mapOf(
               "version" to "/net/mamoe/mirai-console/",
               "jcenter" to "https://jcenter.bintray.com/net/mamoe/mirai-console/{version}/:mirai-console-{version}.jar",
               "aliyun"  to "https://maven.aliyun.com/nexus/content/repositories/jcenter/net/mamoe/mirai-console/{version}/mirai-console-{version}.jar"
           ))
       }
    }

    var consoleType = CONSOLE_PURE

    fun getFile():File?{
        contentPath.listFiles()?.forEach { file ->
            if (file != null && file.extension == "jar") {
                if(file.name.contains("mirai-console")) {
                    when (consoleType) {
                        CONSOLE_PURE -> {
                            return file
                        }
                    }
                }
            }
        }
        return null
    }

    suspend fun versionCheck(type:String) {
        this.consoleType = type
        println("Fetching Newest Console Version of $type")
        val newest = getNewestVersion()
        val current = getCurrentVersion()
        println("Local Console-$type Version: $current | Newest Console-$type Version: $newest")
        if (current != newest) {
            println("Updating Console-$type from V$current -> V$newest, this is a force update")
            this.getFile()?.delete()
            downloadConsole(newest)
            println("Download Console complete")
        }
    }


    private suspend fun getNewestVersion():String{
        try {
            return """>([0-9])*\.([0-9])*\.([0-9])*/""".toRegex().findAll(
                Http.get<String> {
                    url {
                        protocol = URLProtocol.HTTPS
                        host = "jcenter.bintray.com"
                        path(Links[consoleType]!!["version"] ?: error("Unknown Console Type"))
                    }
                }
            ).asSequence()
                .map { it.value.drop(1).dropLast(1) }
                .maxBy {
                    it.split('.').foldRightIndexed(0) { index: Int, s: String, acc: Int ->
                        acc + 100.0.pow(index).toInt() + (s.toIntOrNull() ?: 0)
                    }
                }!!
        } catch (e: Exception) {
            println("Failed to fetch newest Console version, please seek for help")
            e.printStackTrace()
            println("Failed to fetch newest Console version, please seek for help")
            exitProcess(1)
        }
    }

    private fun getCurrentVersion():String{
        val file = getFile()
        if(file != null) {
            val numberVersion = """([0-9])*\.([0-9])*\.([0-9])*""".toRegex().find(file.name)?.value
            if (numberVersion != null) {
                return numberVersion + file.name.substringAfter(numberVersion).substringBefore(".jar")
            }
        }
        return "0.0.0"
    }

    private suspend fun downloadConsole(version:String){
        tryNTimesOrQuit(3,"Failed to download Console, please seek for help") {
            kotlin.runCatching {
                println("Downloading newest Console from Aliyun")
                Http.downloadRequest(Links[consoleType]!!["aliyun"] ?: error("Unknown Console Type"), version)
            }.getOrElse {
                println("Downloading newest Console from JCenter")
                Http.downloadRequest(Links[consoleType]!!["jcenter"] ?: error("Unknown Console Type"), version)
            }
                .saveToContent(if (consoleType == CONSOLE_PURE) {
                    "mirai-console-$version.jar"
                } else {
                    "mirai-console-$consoleType-$version.jar"
                })

        }
    }



}