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


inline fun <R> tryNTimesOrQuit(repeat: Int, errorHint: String, block: (Int) -> R){
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
    println(errorHint)
    lastException!!.printStackTrace()
    println(errorHint)
    exitProcess(1)
}


suspend inline fun HttpClient.downloadRequest(url: String): ByteReadChannel {
    return this.get<HttpResponse>(url).content
}

private val jcenterPath =  "https://jcenter.bintray.com/{group}/{project}/{version}/:{project}-{version}.{extension}"
private val aliyunPath =  "https://maven.aliyun.com/nexus/content/repositories/jcenter/{group}/{project}/{version}/{project}-{version}.{extension}"


suspend fun HttpClient.downloadMaven(
    groupName: String,
    projectName: String,
    version: String,
    extension: String
):ByteReadChannel{
    return kotlin.runCatching {
        downloadRequest(
            aliyunPath
                .replace(
                "{group}",groupName
                )
                .replace(
                "{project}",projectName
                )
                .replace(
                    "{extension}",extension
                )
                .replace(
                    "{version}",version
                )
        )
    }.getOrElse {
        downloadRequest(
            jcenterPath
                .replace(
                    "{group}",groupName
                )
                .replace(
                    "{project}",projectName
                )
                .replace(
                    "{extension}",extension
                )
                .replace(
                    "{version}",version
                )
        )
    }
}

suspend inline fun HttpClient.downloadMavenArchive(
    groupName: String,
    projectName: String,
    version: String
):ByteReadChannel{
    return downloadMaven(groupName,projectName,version,"jar")
}

suspend inline fun HttpClient.downloadMavenPom(
    groupName: String,
    projectName: String,
    version: String
):ByteReadChannel{
    return downloadMaven(groupName,projectName,version,"pom")
}



/**
 * 只要填content path后面的就可以
 */
suspend fun ByteReadChannel.saveToContent(filepath:String){
    val fileStream = File(contentPath.absolutePath + "/" +  filepath).also {
        withContext(Dispatchers.IO) {
            it.createNewFile()
        }
    }.outputStream()

    withContext(Dispatchers.IO) {
        this@saveToContent.copyTo(fileStream)
        fileStream.flush()
    }
}



