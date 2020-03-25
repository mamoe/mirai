@file:Suppress("EXPERIMENTAL_API_USAGE")

package upload

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.HttpTimeout
import io.ktor.client.request.put
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.*

object GitHub {

    private fun getGithubToken(): String {
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

        val property = System.getProperty("github_token", "~")
        if (property == null || property == "~") {
            error(
                "Cannot find github token, " +
                        "please specify by creating a file token.txt in project dir, " +
                        "or by providing JVM parameter 'github_token'"
            )
        }
        return property
    }

    fun upload(file: File, url: String) = runBlocking {
        HttpClient(CIO) {
            engine {
                requestTimeout = 600_000
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 600_000
                requestTimeoutMillis = 600_000
                socketTimeoutMillis = 600_000
            }
        }.put<String>("""$url?access_token=${getGithubToken()}""") {
            val content = String(Base64.getEncoder().encode(file.readBytes()))
            body = """
                    {
                      "message": "automatic upload",
                      "content": "$content"
                    }
                """.trimIndent()
        }
    }
}