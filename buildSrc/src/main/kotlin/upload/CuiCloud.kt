/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package upload

import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.gradle.api.Project
import org.gradle.kotlin.dsl.provideDelegate
import java.io.File
import java.util.*

@Suppress("DEPRECATION")
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

    @UseExperimental(ExperimentalStdlibApi::class)
    private suspend fun uploadToCuiCloud(
        cuiCloudUrl: String,
        cuiToken: String,
        filePath: String,
        content: ByteArray
    ) {
        println("filePath=$filePath")
        println("content=${content.size / 1024 / 1024} MB")

        val response = withContext(Dispatchers.IO) {
            Http.post<HttpResponse>(cuiCloudUrl) {
                body = MultiPartFormDataContent(
                    formData {
                        append("base64", Base64.getEncoder().encodeToString(content))
                        append("filePath", filePath)
                        append("large", "true")
                        append("key", cuiToken)
                    }
                )
            }
        }
        println(response.status)

        val buffer = ByteArray(4096)
        val resp = buildList<Byte> {
            while (true) {
                val read = response.content.readAvailable(buffer, 0, buffer.size)
                if (read == -1) {
                    break
                }
                addAll(buffer.toList().take(read))
            }
        }
        println(String(resp.toByteArray()))

        if (!response.status.isSuccess()) {
            error("Cui cloud response: ${response.status}")
        }
    }
}

inline fun <E> buildList(builderAction: MutableList<E>.() -> Unit): List<E> {
    return ArrayList<E>().apply(builderAction)
}
