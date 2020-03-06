@file:Suppress("EXPERIMENTAL_API_USAGE")
package net.mamoe.mirai.console.wrapper

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.ClientRequestException
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
private fun String.buildPath(
    groupName: String,
    projectName: String,
    version: String,
    extension: String
):String{
    return this
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
}

suspend fun HttpClient.downloadMaven(
    groupName: String,
    projectName: String,
    version: String,
    extension: String
):ByteReadChannel{
    return kotlin.runCatching {
        downloadRequest(
            aliyunPath.buildPath(groupName,projectName,version,extension)
        )
    }.getOrElse {
        downloadRequest(
            aliyunPath.buildPath(groupName,projectName,version,extension)
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

suspend fun HttpClient.downloadMavenPomAsString(
    groupName: String,
    projectName: String,
    version: String
):String{
    return kotlin.runCatching {
        Http.get<String>(
            aliyunPath.buildPath(groupName,projectName,version,"pom")
        )
    }.getOrElse {
       try {
           Http.get(
               aliyunPath.buildPath(groupName, projectName, version, "pom")
           )
       }catch (e:Exception){
           if(e.message?.contains("404 Not Found") == true) {
               return ""
           }
           throw e
       }
    }
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



