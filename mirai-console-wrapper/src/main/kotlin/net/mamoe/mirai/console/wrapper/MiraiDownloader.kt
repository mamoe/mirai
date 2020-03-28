package net.mamoe.mirai.console.wrapper

import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.system.exitProcess


internal object MiraiDownloader{
    private val tasks = mutableMapOf<String,File>()

    fun addTask(
        fromUrl: String,
        to: File
    ){
        tasks[fromUrl] = to
    }

    suspend fun downloadIfNeed(){
        if(tasks.isNotEmpty()){
            MiraiDownloaderImpl(EmptyCoroutineContext, tasks).waitUntilFinish()
        }
    }
}

private class MiraiDownloaderImpl(
    override val coroutineContext: CoroutineContext = EmptyCoroutineContext,
    tasks: Map<String, File>
):CoroutineScope {

    val bar = MiraiDownloaderProgressBar()

    var totalDownload = AtomicInteger(0)
    var totalSize     = AtomicInteger(0)

    private var isDownloadFinish: Job

    init {
        println("Mirai Downloader")
        println("[Mirai国内镜像] 感谢崔Cloud慷慨提供免费的国内储存分发")
        isDownloadFinish = this.async {
            tasks.forEach {
                this.launch {
                    downloadTask(it.key, it.value)
                }
            }
        }
    }

    suspend fun waitUntilFinish(){
        while (!isDownloadFinish.isCompleted){
            bar.update(totalDownload.get().toFloat()/totalSize.get(),(totalSize.get()/(1024*1024)).toString() +   "MB" )
            delay(50)
        }
        bar.update(1F,"Complete")
        bar.complete()
    }


    @Throws(Exception::class)
    private suspend fun downloadTask(fromUrl: String, file: File) {
        withContext(Dispatchers.IO) {
            try {
                val con = URL(fromUrl).openConnection() as HttpURLConnection
                val input= con.inputStream
                totalSize.addAndGet(con.contentLength)
                val outputStream = FileOutputStream(file)
                var len = -1
                val buff = ByteArray(1024)
                while (input.read(buff).also { len = it } != -1) {
                    totalDownload.addAndGet(buff.size)
                    outputStream.write(buff, 0, len)
                }
            }catch (e: Exception){
                bar.update(1F,"Failed")
                bar.complete()
                println("Failed to download resources from " + fromUrl + " reason " + e.message)
                e.printStackTrace()
                println("Failed to download resources from " + fromUrl + " reason " + e.message)
                println("Please Seek For Help")
                exitProcess(1)
            }
        }
    }
}


class MiraiDownloaderProgressBar(){

    private fun reset() {
        print('\r')
    }

    private val barLen = 40

    fun update(rate: Float, message: String) {
        reset()
        print("Progress: ")
        val len =  (rate * barLen).toInt()
        for (i in 0 until len) {
            print("#")
        }
        for (i in 0 until barLen - len) {
            print(" ")
        }
        print("  | $message")
    }

    fun complete(){
        println()
    }
}





