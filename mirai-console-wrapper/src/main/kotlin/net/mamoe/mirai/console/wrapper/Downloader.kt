@file:Suppress("EXPERIMENTAL_API_USAGE")
package net.mamoe.mirai.console.wrapper

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

import kotlin.system.exitProcess

val Http: HttpClient
    get() = HttpClient(CIO)


inline fun <R> tryNTimesOrQuit(repeat: Int, block: (Int) -> R){
    var lastException: Throwable? = null

    repeat(repeat) {
        try {
            block(it)
            return
        } catch (e: Throwable) {
            if (lastException == null) {
                lastException = e
            } else lastException!!.addSuppressed(e)
        }
    }

    lastException!!.printStackTrace()
    exitProcess(1)
}


suspend inline fun HttpClient.downloadRequest(url: String, version: String): ByteReadChannel {
    return this.get<HttpResponse>(url.replace("{version}", version)).content
}

/**
 * 只要填content path后面的就可以
 */
suspend fun ByteReadChannel.saveTo(filepath:String){
    val fileStream = File(contentPath.absolutePath + "/" +  filepath).also {
        withContext(Dispatchers.IO) {
            it.createNewFile()
        }
    }.outputStream()

    withContext(Dispatchers.IO) {
        this@saveTo.copyTo(fileStream)
        fileStream.flush()
    }
}

