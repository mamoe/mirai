/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.console.wrapper

import io.ktor.client.request.get
import io.ktor.http.URLProtocol
import java.io.File
import kotlin.math.pow
import kotlin.system.exitProcess

internal object CoreUpdater {

    fun getProtocolLib(): File? {
        contentPath.listFiles()?.forEach { file ->
            if (file != null && file.extension == "jar" && file.name.contains("qqandroid")) {
                return file
            }
        }
        return null
    }


    suspend fun versionCheck() {
        println("Fetching Newest Core Version .. ")
        val newest = getNewestVersion()
        val current = getCurrentVersion()
        println("Local Core Version: $current | Newest Core Version: $newest")
        if (current != newest) {
            println("Updating shadowed-core from V$current -> V$newest, this is a force update")
            this.getProtocolLib()?.delete()
            MiraiDownloader
                .addTask(
                    "https://pan.jasonczc.cn/?/mirai/mirai-core-qqandroid/mirai-core-qqandroid-$newest.mp4",
                    getContent("mirai-core-qqandroid-jvm-$newest.jar")
                )
            //.addTask("https://raw.githubusercontent.com/mamoe/mirai-repo/master/shadow/mirai-core-qqandroid/mirai-core-qqandroid-$newest.jar", getContent("mirai-core-qqandroid-jvm-$newest.jar"))

        }
    }

    /**
     * 判断最新版本
     * */
    private suspend fun getNewestVersion(): String {
        try {
            return """>([0-9])*\.([0-9])*\.([0-9])*/""".toRegex().findAll(
                    Http.get<String> {
                        url {
                            protocol = URLProtocol.HTTPS
                            host = "jcenter.bintray.com"
                            path("net/mamoe/mirai-core-qqandroid/")
                        }
                    }).asSequence()
                .map { it.value.drop(1).dropLast(1) }
                .maxBy {
                    it.split('.').foldRightIndexed(0) { index: Int, s: String, acc: Int ->
                        acc + 100.0.pow(2 - index).toInt() * (s.toIntOrNull() ?: 0)
                    }
                }!!
        } catch (e: Exception) {
            println("Failed to fetch newest Core version, please seek for help")
            e.printStackTrace()
            println("Failed to fetch newest Core version, please seek for help")
            exitProcess(1)
        }
    }

    /**
     * 判断当前版本
     * 默认返回 "0.0.0"
     */
    fun getCurrentVersion(): String {
        val file = getProtocolLib() ?: return "0.0.0"
        val numberVersion = """([0-9])*\.([0-9])*\.([0-9])*""".toRegex().find(file.name)?.value
        if (numberVersion != null) {
            return numberVersion + file.name.substringAfter(numberVersion).substringBefore(".jar")
        }
        return "0.0.0"
    }


    /*
    private suspend fun downloadCore(version: String) {
        /**
         * from github
         */


        /**
         * from jcenter
        coroutineScope {
            launch {
                tryNTimesOrQuit(3, "Failed to download newest Protocol lib, please seek for help") {
                    Http.downloadMavenArchive("net/mamoe", "mirai-core-qqandroid-jvm", version)
                        .saveToContent("mirai-core-qqandroid-jvm-$version.jar")
                }
            }

            launch {
                tryNTimesOrQuit(3, "Failed to download newest core, please seek for help") {
                    Http.downloadMavenArchive("net/mamoe", "mirai-core-jvm", version)
                        .saveToContent("mirai-core-jvm-$version.jar")
                }
            }
        }
        */
    }


     */


}