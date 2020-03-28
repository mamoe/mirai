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

    suspend fun downloadIfNeed(isUI:Boolean){
        if(tasks.isNotEmpty()){
            if(!isUI) {
                MiraiDownloaderImpl(EmptyCoroutineContext, tasks, false, MiraiDownloaderProgressBarInTerminal()).waitUntilFinish()
            }else{
                MiraiDownloaderImpl(EmptyCoroutineContext, tasks, false, MiraiDownloaderProgressBarInUI()).waitUntilFinish()
            }
        }
    }
}

//background => any print
private class MiraiDownloaderImpl(
    override val coroutineContext: CoroutineContext = EmptyCoroutineContext,
    tasks: Map<String, File>,
    val background:Boolean,
    val bar:MiraiDownloadProgressBar
):CoroutineScope {

    fun log(any:Any?){
        if(!background && any != null){
            println(background)
        }
    }

    var totalDownload = AtomicInteger(0)
    var totalSize     = AtomicInteger(0)

    private var isDownloadFinish: Job

    init {
        bar.ad()
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


interface MiraiDownloadProgressBar{
    fun reset()
    fun update(rate: Float, message: String)
    fun complete()
    fun ad()
}

class MiraiDownloaderProgressBarInTerminal(): MiraiDownloadProgressBar{

    override fun reset() {
        print('\r')
    }

    override fun ad(){
        println("Mirai Downloader")
        println("[Mirai国内镜像] 感谢崔Cloud慷慨提供免费的国内储存分发")
    }
    private val barLen = 40

    override fun update(rate: Float, message: String) {
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

    override fun complete(){
        println()
    }
}

class MiraiDownloaderProgressBarInUI(): MiraiDownloadProgressBar{

    override fun reset() {
        WrapperMain.uiBarOutput.clear()
    }

    override fun ad(){
        WrapperMain.uiLog("[Mirai国内镜像] 感谢崔Cloud慷慨提供更新服务器")
    }
    private val barLen = 20

    override fun update(rate: Float, message: String) {
        reset()
        WrapperMain.uiBarOutput.append("Progress: ")
        val len =  (rate * barLen).toInt()
        for (i in 0 until len) {
            WrapperMain.uiBarOutput.append("#")
        }
        for (i in 0 until barLen - len) {
            WrapperMain.uiBarOutput.append(" ")
        }
        WrapperMain.uiBarOutput.append("  | $message")
    }

    override fun complete() {
        TODO("Not yet implemented")
    }

}


