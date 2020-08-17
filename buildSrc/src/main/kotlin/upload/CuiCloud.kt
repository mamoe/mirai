/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package upload

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.gradle.api.Project
import org.gradle.kotlin.dsl.provideDelegate
import java.io.File
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

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


        val bytes = file.readBytes()

        runBlocking {
            var first = true
            retryCatching(1000) {
                if (!first) {
                    println()
                    println()
                    println("Upload failed. Waiting 15s")
                    delay(15_000)
                }
                first = false
                uploadToCuiCloud(
                    cuiCloudUrl,
                    key,
                    "/mirai/${project.name}/${file.nameWithoutExtension}.mp4",
                    bytes
                )
            }.getOrThrow()
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


@OptIn(ExperimentalContracts::class)
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "RESULT_CLASS_IN_RETURN_TYPE")
@kotlin.internal.InlineOnly
internal inline fun <R> retryCatching(n: Int, onFailure: () -> Unit = {}, block: () -> R): Result<R> {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }
    require(n >= 0) { "param n for retryCatching must not be negative" }
    var exception: Throwable? = null
    repeat(n) {
        try {
            return Result.success(block())
        } catch (e: Throwable) {
            try {
                exception?.addSuppressed(e)
            } catch (e: Throwable) {
            }
            exception = e
            onFailure()
        }
    }
    return Result.failure(exception!!)
}

inline fun <E> buildList(builderAction: MutableList<E>.() -> Unit): List<E> {
    return ArrayList<E>().apply(builderAction)
}
