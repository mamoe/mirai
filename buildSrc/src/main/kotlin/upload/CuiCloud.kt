/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package upload

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.gradle.api.Project
import org.gradle.kotlin.dsl.provideDelegate
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.File
import java.util.*

object CuiCloud {
    private fun getUrl(project: Project): String {
        kotlin.runCatching {
            @Suppress("UNUSED_VARIABLE", "LocalVariableName")
            val cui_cloud_url: String by project
            return cui_cloud_url
        }

        System.getProperty("cui_cloud_url", null)?.let {
            return it.trim()
        }
        File(File(System.getProperty("user.dir")).parent, "/cuiUrl.txt").let { local ->
            if (local.exists()) {
                return local.readText().trim()
            }
        }

        File(File(System.getProperty("user.dir")), "/cuiUrl.txt").let { local ->
            if (local.exists()) {
                return local.readText().trim()
            }
        }
        error("cannot find url for CuiCloud")
    }

    private fun getKey(project: Project): String {
        kotlin.runCatching {
            @Suppress("UNUSED_VARIABLE", "LocalVariableName")
            val cui_cloud_key: String by project
            return cui_cloud_key
        }

        System.getProperty("cui_cloud_key", null)?.let {
            return it.trim()
        }
        File(File(System.getProperty("user.dir")).parent, "/cuiToken.txt").let { local ->
            if (local.exists()) {
                return local.readText().trim()
            }
        }

        File(File(System.getProperty("user.dir")), "/cuiToken.txt").let { local ->
            if (local.exists()) {
                return local.readText().trim()
            }
        }

        error("cannot find key for CuiCloud")
    }

    fun upload(file: File, project: Project) {
        val cuiCloudUrl = getUrl(project)
        val key = getKey(project)

        runBlocking {
            uploadToCuiCloud(
                cuiCloudUrl,
                key,
                "/mirai/${project.name}/${file.nameWithoutExtension}.mp4",
                file.readBytes()
            )
        }
    }

    private suspend fun uploadToCuiCloud(
        cuiCloudUrl: String,
        cuiToken: String,
        filePath: String,
        content: ByteArray
    ) {
        val response = withContext(Dispatchers.IO) {
            Jsoup.connect(cuiCloudUrl).method(Connection.Method.POST)
                .data("base64", Base64.getEncoder().encodeToString(content))
                .data("filePath", filePath)
                .data("key", cuiToken)
                .timeout(Int.MAX_VALUE)
                .execute()
        }
        if (response.statusCode() != 200) {
            println(response.body())
            error("Cui Cloud Does Not Return 200")
        }
    }
}