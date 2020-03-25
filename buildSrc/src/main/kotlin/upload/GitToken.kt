@file:Suppress("EXPERIMENTAL_API_USAGE")

package upload

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.timeout
import io.ktor.client.request.put
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.*

object GitToken {

    private fun getGitToken(): String {
        with(File(System.getProperty("user.dir")).parent + "/token.txt") {
            println("reading token file in $this")
            return File(this).readText()
        }
    }

    fun upload(file: File, url: String) = runBlocking {
        HttpClient(CIO) {
            install(HttpTimeout)
        }.put<String>("""$url?access_token=${getGitToken()}""") {
            timeout {
                connectTimeoutMillis = 600_000
                requestTimeoutMillis = 600_000
                socketTimeoutMillis = 600_000
            }
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