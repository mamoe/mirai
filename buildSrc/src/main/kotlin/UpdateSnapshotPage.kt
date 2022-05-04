/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */


import com.google.gson.JsonObject
import org.gradle.api.Project
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object UpdateSnapshotPage {
    fun safeToStr(any: Any?): String = any.toString()

    fun run(project: Project, sha: String) {
        val token = System.getenv("GH_TOKEN") ?: error("GH_TOKEN not found")

        val ver = safeToStr(project.version)
        val http = HttpClient.newHttpClient()
        val document = project.rootProject.projectDir.resolve("docs/UsingSnapshots.md").let { file ->
            kotlin.runCatching { file.readText() }.getOrElse { "" }
        }
        val content = JsonObject().also { data ->
            data.addProperty("name", "Snapshot Build Output")
            data.addProperty("head_sha", sha.trim())
            data.addProperty("conclusion", "success")
            data.add("output", JsonObject().also { output ->
                output.addProperty("title", "Snapshot build ($ver)")
                output.addProperty("summary", "snapshot version: `$ver`\n\n------\n\n\n$document")
            })
        }.toString()
        http.send(
            HttpRequest.newBuilder(URI.create("https://api.github.com/repos/mamoe/mirai/check-runs"))
                .POST(HttpRequest.BodyPublishers.ofString(content))
                .header("Authorization", "token $token")
                .header("Accept", "application/vnd.github.v3+json")
                .build(),
            HttpResponse.BodyHandlers.ofByteArrayConsumer { rsp ->
                if (rsp.isPresent) {
                    System.out.write(rsp.get())
                } else {
                    println()
                    println()
                }
            }
        )

        (http.executor().orElse(null) as? java.util.concurrent.ExecutorService)?.shutdown()
    }
}