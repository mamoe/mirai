/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:OptIn(ConsoleExperimentalAPI::class)

package net.mamoe.mirai.console.internal.plugin

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.mamoe.mirai.console.plugin.center.PluginCenter
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.console.util.retryCatching
import java.io.File

internal val json = Json {
    isLenient = true
    ignoreUnknownKeys = true
}

@OptIn(KtorExperimentalAPI::class)
internal val Http = HttpClient(CIO)

internal object CuiPluginCenter : PluginCenter {

    var plugins: List<PluginCenter.PluginInsight>? = null

    /**
     * 一页 10 个 pageMinNum=1
     */
    override suspend fun fetchPlugin(page: Int): Map<String, PluginCenter.PluginInsight> {
        check(page > 0)
        val startIndex = (page - 1) * 10
        val endIndex = startIndex + 9
        val map = mutableMapOf<String, PluginCenter.PluginInsight>()
        (startIndex until endIndex).forEach { index ->
            val plugins = plugins ?: kotlin.run {
                refresh()
                plugins
            } ?: return mapOf()

            if (index >= plugins.size) {
                return@forEach
            }

            map[name] = plugins[index]
        }
        return map
    }

    override suspend fun findPlugin(name: String): PluginCenter.PluginInfo? {
        val result = retryCatching(3) {
            Http.get<String>("https://miraiapi.jasonczc.cn/getPluginDetailedInfo?name=$name")
        }.getOrElse { return null }
        if (result == "err:not found") return null

        return json.decodeFromString(PluginCenter.PluginInfo.serializer(), result)
    }

    override suspend fun refresh() {

        @Serializable
        data class Result(
            val success: Boolean,
            val result: List<PluginCenter.PluginInsight>
        )

        val result = json.decodeFromString(Result.serializer(), Http.get("https://miraiapi.jasonczc.cn/getPluginList"))

        check(result.success) { "Failed to fetch plugin list from Cui Cloud" }
        plugins = result.result
    }

    override suspend fun <T : Any> T.downloadPlugin(name: String, progressListener: T.(Float) -> Unit): File {
        TODO()
        /*
        val info = findPlugin(name) ?: error("Plugin Not Found")
        val targetFile = File(PluginManager.pluginsPath, "$name-" + info.version + ".jar")
        withContext(Dispatchers.IO) {
            tryNTimes {
                val con =
                    URL("https://pan.jasonczc.cn/?/mirai/plugins/$name/$name-" + info.version + ".mp4").openConnection() as HttpURLConnection
                val input = con.inputStream
                val size = con.contentLength
                var totalDownload = 0F
                val outputStream = FileOutputStream(targetFile)
                var len: Int
                val buff = ByteArray(1024)
                while (input.read(buff).also { len = it } != -1) {
                    totalDownload += len
                    outputStream.write(buff, 0, len)
                    progressListener.invoke(this@downloadPlugin, totalDownload / size)
                }
            }
        }
        return targetFile
         */
    }

    override val name: String get() = "崔云"
}

