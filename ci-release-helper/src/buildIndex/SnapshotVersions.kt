/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
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
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

object GetNextSnapshotIndex {
    @JvmStatic
    fun main(args: Array<String>) {
        val branch = args.getOrNull(0)?.replace(Regex("""[/\\.,`~!@#$%^&*(){}\[\]|;]"""), "-")
            ?: error("Missing branch argument")
        val commitRef = args.getOrNull(1) ?: error("Missing commitRef argument")


        println("Commit ref is: $commitRef")
        println("Making request...")
        HttpClient().use { client ->
            runBlocking {
                kotlin.runCatching { client.createBranch(branch = branch) }

                var index = client.getExistingIndex(branch = branch, commitRef = commitRef)
                if (index == null) {
                    print("No existing index found. ")
                    index = client.postNextIndex(branch = branch, commitRef = commitRef)
                    println("Got new index: $index")
                } else {
                    print("Existing index: $index")
                }
                println()

                println("<SNAPSHOT_VERSION_START>$branch-${index.value}<SNAPSHOT_VERSION_END>")
            }
        }
    }
}

suspend fun HttpClient.getExistingIndex(
    module: String = "mirai-core",
    branch: String,
    commitRef: String,
): Index? {
    // https://build.mirai.mamoe.net/v1/mirai-core/dev/indexes/?commitRef=29121565132bed6e996f3de32faaf49106ae8e39
    val resp =
        get("https://build.mirai.mamoe.net/v1/${module.encodeURLPathPart()}/${branch.encodeURLPathPart()}/indexes/") {
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
    if (body.isBlank()) return null
    return Json.decodeFromString(ListSerializer(Index.serializer()), body).lastOrNull()
}

suspend fun HttpClient.createBranch(
    module: String = "mirai-core",
    branch: String,
): Boolean {
    // https://build.mirai.mamoe.net/v1/mirai-core/dev/indexes/?commitRef=29121565132bed6e996f3de32faaf49106ae8e39
    val resp = put("https://build.mirai.mamoe.net/v1/${module.encodeURLPathPart()}/${branch.encodeURLPathPart()}") {
        basicAuth(
            System.getenv("mirai.build.index.auth.username"),
            System.getenv("mirai.build.index.auth.password")
        )
    }
    return resp.status.isSuccess()
}

suspend fun HttpClient.postNextIndex(
    module: String = "mirai-core",
    branch: String,
    commitRef: String,
): Index {
    val resp =
        post("https://build.mirai.mamoe.net/v1/${module.encodeURLPathPart()}/${branch.encodeURLPathPart()}/indexes/next") {
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