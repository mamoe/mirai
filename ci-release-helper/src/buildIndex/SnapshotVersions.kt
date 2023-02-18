/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package cihelper.buildIndex

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

object GetNextSnapshotIndex {
    @JvmStatic
    fun main(args: Array<String>) {
        val commitRef = args.getOrNull(0) ?: error("Missing commitRef argument")

        println("Commit ref is: $commitRef")
        println("Making request...")
        HttpClient().use { client ->
            val index = runBlocking { client.postNextIndex(commitRef = commitRef) }
            println(index)
            println()

            println("<SNAPSHOT_VERSION_START>${index.value}<SNAPSHOT_VERSION_END>")
        }
    }
}

suspend fun HttpClient.postNextIndex(
    module: String = "mirai-core",
    branch: String = "dev",
    commitRef: String,
): Index {
    val resp = post("https://build.mirai.mamoe.net/v1/$module/$branch/indexes/next") {
        basicAuth(
            System.getenv("mirai.build.index.auth.username"),
            System.getenv("mirai.build.index.auth.password")
        )
        parameter("commitRef", commitRef)
    }
    if (!resp.status.isSuccess()) {
        val body = runCatching { resp.bodyAsText() }.getOrNull()
        throw IllegalStateException("Request failed: ${resp.status}  $body")
    }

    val body = resp.bodyAsText()
    return Json.decodeFromString(NextIndexResp.serializer(), body).newIndex
}