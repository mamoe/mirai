/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package upload

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.HttpTimeout
import io.ktor.client.request.put
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.gradle.api.Project
import org.gradle.kotlin.dsl.provideDelegate
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.File
import java.util.*

internal val Http = HttpClient(CIO) {
    engine {
        requestTimeout = 600_000
    }
    install(HttpTimeout) {
        socketTimeoutMillis = 600_000
        requestTimeoutMillis = 600_000
        connectTimeoutMillis = 600_000
    }
}

object GitHub {

    private fun getGithubToken(project: Project): String {
        kotlin.runCatching {
            @Suppress("UNUSED_VARIABLE", "LocalVariableName")
            val github_token: String by project
            return github_token
        }

        System.getProperty("github_token", null)?.let {
            return it.trim()
        }

        File(File(System.getProperty("user.dir")).parent, "/token.txt").let { local ->
            if (local.exists()) {
                return local.readText().trim()
            }
        }

        File(File(System.getProperty("user.dir")), "/token.txt").let { local ->
            if (local.exists()) {
                return local.readText().trim()
            }
        }

        error(
            "Cannot find github token, " +
                    "please specify by creating a file token.txt in project dir, " +
                    "or by providing JVM parameter 'github_token'"
        )
    }

    fun upload(file: File, project: Project, repo: String, targetFilePath: String) = runBlocking {
        val token = getGithubToken(project)
        println("token.length=${token.length}")
        val url = "https://api.github.com/repos/project-mirai/$repo/contents/$targetFilePath"
        retryCatching(100, onFailure = { delay(30_000) }) { // 403 forbidden?
            Http.put<String>("$url?access_token=$token") {
                val sha = retryCatching(3, onFailure = { delay(30_000) }) {
                    getGithubSha(
                        repo,
                        targetFilePath,
                        "master",
                        project
                    )
                }.getOrNull()
                println("sha=$sha")
                val content = String(Base64.getEncoder().encode(file.readBytes()))
                body = """
                    {
                      "message": "automatically upload on release",
                      "content": "$content"
                      ${if (sha == null) "" else """, "sha": "$sha" """}
                    }
                """.trimIndent()
            }.let {
                println("Upload response: $it")
            }
            delay(1000)
        }.getOrThrow()
    }


    private suspend fun getGithubSha(
        repo: String,
        filePath: String,
        branch: String,
        project: Project
    ): String? {
        fun String.asJson(): JsonObject {
            return JsonParser.parseString(this).asJsonObject
        }

        /*
        * 只能获取1M以内/branch为master的sha
        * */
        class TargetTooLargeException : Exception("Target TOO Large")

        suspend fun getShaSmart(repo: String, filePath: String, project: Project): String? {
            return withContext(Dispatchers.IO) {
                val response = Jsoup
                    .connect(
                        "https://api.github.com/repos/project-mirai/$repo/contents/$filePath?access_token=" + getGithubToken(
                            project
                        )
                    )
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .method(Connection.Method.GET)
                    .execute()
                if (response.statusCode() == 404) {
                    null
                } else {
                    val p = response.body().asJson()
                    if (p.has("message") && p["message"].asString == "This API returns blobs up to 1 MB in size. The requested blob is too large to fetch via the API, but you can use the Git Data API to request blobs up to 100 MB in size.") {
                        throw TargetTooLargeException()
                    }
                    p.get("sha").asString
                }
            }
        }

        suspend fun getShaStupid(
            repo: String,
            filePath: String,
            branch: String,
            project: Project
        ): String? {
            val resp = withContext(Dispatchers.IO) {
                Jsoup
                    .connect(
                        "https://api.github.com/repos/project-mirai/$repo/git/ref/heads/$branch?access_token=" + getGithubToken(
                            project
                        )
                    )
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .method(Connection.Method.GET)
                    .execute()
            }
            if (resp.statusCode() == 404) {
                println("Branch Not Found")
                return null
            }
            val info = resp.body().asJson().get("object").asJsonObject.get("url").asString
            var parentNode = withContext(Dispatchers.IO) {
                Jsoup.connect(info + "?access_token=" + getGithubToken(project)).ignoreContentType(true)
                    .method(Connection.Method.GET)
                    .execute().body().asJson().get("tree").asJsonObject.get("url").asString
            }
            filePath.split("/").forEach { subPath ->
                withContext(Dispatchers.IO) {
                    Jsoup.connect(parentNode + "?access_token=" + getGithubToken(project)).ignoreContentType(true)
                        .method(Connection.Method.GET).execute().body().asJson().get("tree").asJsonArray
                }.forEach list@{
                    with(it.asJsonObject) {
                        if (this.get("path").asString == subPath) {
                            parentNode = this.get("url").asString
                            return@list
                        }
                    }
                }
            }
            check(parentNode.contains("/blobs/"))
            return parentNode.substringAfterLast("/")
        }

        return if (branch == "master") {
            try {
                getShaSmart(repo, filePath, project)
            } catch (e: TargetTooLargeException) {
                getShaStupid(repo, filePath, branch, project)
            }
        } else {
            getShaStupid(repo, filePath, branch, project)
        }
    }
}