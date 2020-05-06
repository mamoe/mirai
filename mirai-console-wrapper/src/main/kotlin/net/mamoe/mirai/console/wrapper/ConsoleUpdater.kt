/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
package net.mamoe.mirai.console.wrapper

import io.ktor.client.request.get
import io.ktor.http.URLProtocol
import java.io.File
import kotlin.math.pow
import kotlin.system.exitProcess

const val CONSOLE_PURE = "Pure"
const val CONSOLE_TERMINAL = "Terminal"
const val CONSOLE_GRAPHICAL = "Graphical"


internal object ConsoleUpdater {

    @Suppress("SpellCheckingInspection")
    private object Links : HashMap<ConsoleType, Map<String, String>>() {
        init {
            put(
                ConsoleType.Pure, mapOf(
                    "version" to "/net/mamoe/mirai-console/"
                )
            )
            put(
                ConsoleType.Graphical, mapOf(
                    "version" to "/net/mamoe/mirai-console-graphical/"
                )
            )
        }
    }


    var consoleType = ConsoleType.Pure

    fun getFile(): File? {
        contentPath.listFiles()?.forEach { file ->
            if (file != null && file.extension == "jar") {
                if (file.name.contains("mirai-console")) {
                    when (consoleType) {
                        ConsoleType.Pure -> {
                            if (!file.name.contains("pure")) {
                                return file
                            }
                        }
                        ConsoleType.Graphical -> {
                            if (file.name.contains("graphical")) {
                                return file
                            }
                        }
                        else -> {

                        }
                    }
                }
            }
        }
        return null
    }

    suspend fun versionCheck(type: ConsoleType, strategy: VersionUpdateStrategy) {
        this.consoleType = type
        println("Fetching Newest Console Version of $type")
        val current = CoreUpdater.getCurrentVersion()
        if (current != "0.0.0" && strategy == VersionUpdateStrategy.KEEP) {
            println("Stay on current version.")
            return
        }

        val newest = getNewestVersion(
            strategy,
            Links[consoleType]!!["version"] ?: error("Unknown Console Type")
        )
        println("Local Console-$type Version: $current | Newest $strategy Console-$type Version: $newest")
        if (current != newest) {
            println("Updating Console-$type from V$current -> V$newest")
            this.getFile()?.delete()
            /**
            MiraiDownloader.addTask(
            "https://raw.githubusercontent.com/mamoe/mirai-repo/master/shadow/${getProjectName()}/${getProjectName()}-$newest.jar",getContent("${getProjectName()}-$newest.jar")
            )
             */
            MiraiDownloader.addTask(
                "https://pan.jasonczc.cn/?/mirai/${getProjectName()}/${getProjectName()}-$newest.mp4",
                getContent("${getProjectName()}-$newest.jar")
            )
        }
    }

    fun getCurrentVersion(): String {
        val file = getFile()
        if (file != null) {
            val numberVersion = """([0-9])*\.([0-9])*\.([0-9])*""".toRegex().find(file.name)?.value
            if (numberVersion != null) {
                return numberVersion + file.name.substringAfter(numberVersion).substringBefore(".jar")
            }
        }
        return "0.0.0"
    }

    private fun getProjectName(): String {
        return if (consoleType == ConsoleType.Pure) {
            "mirai-console"
        } else {
            "mirai-console-${consoleType.toString().toLowerCase()}"
        }
    }

}


suspend fun getNewestVersion(strategy: VersionUpdateStrategy, path: String): String {
    try {
        return Regex("""rel="nofollow">[0-9][0-9]*(\.[0-9]*)*.*/<""", RegexOption.IGNORE_CASE).findAll(
                Http.get<String> {
                    url {
                        protocol = URLProtocol.HTTPS
                        host = "jcenter.bintray.com"
                        path(path)
                    }
                })
            .asSequence()
            .map { it.value.substringAfter('>').substringBefore('/') }
            .toList()
            .let { list ->
                if (list.filter { it.startsWith("1.") }.takeIf { it.isNotEmpty() }?.all { it.contains("-") } == true) {
                    // 只有 1.xxx-EA 版本, 那么也将他看作是正式版
                    list.filter { it.startsWith("1.") }
                } else when (strategy) {
                    VersionUpdateStrategy.KEEP,
                    VersionUpdateStrategy.STABLE
                    -> {
                        list.filterNot { it.contains("-") } // e.g. "-EA"
                    }
                    VersionUpdateStrategy.EA -> {
                        list
                    }
                }
            }
            .latestVersion()
    } catch (e: Exception) {
        println("Failed to fetch newest Console version, please seek for help")
        e.printStackTrace()
        println("Failed to fetch newest Console version, please seek for help")
        exitProcess(1)
    }
}

internal fun List<String>.latestVersion(): String {
    return sortByVersion().first()
}

internal fun List<String>.sortByVersion(): List<String> {
    return sortedByDescending { version ->
        version.split('.').let {
            if (it.size == 2) it + "0"
            else it
        }.reversed().foldIndexed(0.0) { index: Int, acc: Double, s: String ->
            acc + 1000.0.pow(index) * s.convertPatchVersionToWeight()
        }
    }
}

internal fun String.convertPatchVersionToWeight(): Double {
    return this.split('-').reversed().foldIndexed(0.0) { index: Int, acc: Double, s: String ->
        acc + 10.0.pow(index) * (s.toIntOrNull()?.toDouble() ?: -0.5)
    }
}