package demo.gentleman

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import net.mamoe.ex.content.RandomAccessHDImage
import net.mamoe.ex.network.ExNetwork
import net.mamoe.ex.network.connections.defaults.DownloadHDImageStreamSpider
import net.mamoe.ex.network.connections.defaults.ExIPWhiteListSpider
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.sendTo
import net.mamoe.mirai.message.uploadImage
import net.mamoe.robot.AsyncTaskPool
import java.io.Closeable
import java.io.InputStream
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future


/**
 * 最少缓存的图片数量
 */
private const val IMAGE_BUFFER_CAPACITY: Int = 50

/**
 * 每次补充的数量
 */
private const val FILL_COUNT: Int = IMAGE_BUFFER_CAPACITY

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
object Gentlemen : MutableMap<UInt, Gentleman> by mutableMapOf() {
    fun getOrPut(key: Contact): Gentleman = this.getOrPut(key.id) { Gentleman(key) }
}


private val sessionMap = LinkedHashMap<Long, HPictureSession>()

val ERROR_LINK = "https://i.loli.net/2019/08/05/usINjXSiZxrQJkT.jpg"
val TITLE_PICTURE_LINK = "https://i.loli.net/2019/08/04/B5ZMw1rdzVQI7Yv.jpg"

var minstar = 100

class HPictureSession constructor(private val group: Group, private val sender: QQ, val keyword: String) : Closeable {

    private var hdImage: RandomAccessHDImage? = null
    var sentCount: Int = 0
        set(sentCount) {
            field = this.sentCount
        }//已经发送了几个 ImageSet

    private var fetchTask: Future<RandomAccessHDImage>? = null

    init {
        AsyncTaskPool.submit {
            try {
                Thread.sleep((1000 * 60 * 10).toLong())//10min
            } catch (ignored: InterruptedException) {
            }

            close()
        }
    }

    init {
        GlobalScope.launch { reloadImage() }
    }

    private suspend fun reloadImage() {
        if (keyword.isEmpty()) {
            group.sendMessage("正在搜寻随机色图")
        } else {
            group.sendMessage("正在搜寻有关 $keyword 的色图")
        }
        try {
            withContext(IO) {
                if (!ExNetwork.doSpider(ExIPWhiteListSpider()).get()) {
                    group.sendMessage("无法连接EX")
                    close()
                    return@withContext
                }
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
            close()
            return
        } catch (e: ExecutionException) {
            e.printStackTrace()
            close()
            return
        }

        this.fetchTask = ExNetwork.getRandomImage(keyword, minstar) { value ->
            this.hdImage = value
            if (this.hdImage == null) {
                runBlocking { group.sendMessage("没找到") }
                close()
            } else {
                with(this.hdImage!!) {
                    if (this.picId != null) {
                        runBlocking {
                            group.sendMessage(picId)
                        }
                    } else {
                        AsyncTaskPool.submit {
                            try {
                                runBlocking {
                                    group.uploadImage(ExNetwork.doSpider(DownloadHDImageStreamSpider(this@with)).get() as InputStream).sendTo(group)
                                }
                            } catch (var7: Exception) {
                                var7.printStackTrace()
                            }

                        }
                    }
                }

            }
        }
    }

    override fun close() {
        this.hdImage = null
        if (this.fetchTask != null) {
            if (!this.fetchTask!!.isCancelled && !this.fetchTask!!.isDone) {
                this.fetchTask!!.cancel(true)
            }
        }
        this.fetchTask = null
        sessionMap.entries.removeIf { longHPictureSessionEntry -> longHPictureSessionEntry.value === this }
    }
}

@ExperimentalCoroutinesApi
class Gentleman(private val contact: Contact) : Channel<GentleImage> by Channel(IMAGE_BUFFER_CAPACITY) {
    init {

        GlobalScope.launch {
            while (!isClosedForSend) {
                send(GentleImage().apply {
                    sample_url = "http://dev.itxtech.org:10322/randomImg.uue?tdsourcetag=s_pctim_aiomsg&size=large"
                    contact = this@Gentleman.contact

                    image.await()
                })

/*
                val response = withContext(Dispatchers.IO) {
                    tryNTimes(2) {
                        Jsoup.connect("https://yande.re/post.json?")
                            .userAgent(UserAgent.randomUserAgent)
                            .data("limit", "20")
                            .data("page", (Random.Default.nextInt(12000)).toString())
                            .ignoreContentType(true)
                            .timeout(20_000)
                            .method(Connection.Method.GET)
                            .execute()
                    }
                }
                check(response.statusCode() == 200) { "failed to get resources" }

                JSONArray.parseArray(response.body(), GentleImage::class.java)
                    .filterNot { it.tags in ForbiddenKeyWords }
                    .sortedBy { it.score }
                    .let {
                        if (it.size <= FILL_COUNT) {
                            it
                        } else it.slice(0..FILL_COUNT)
                    }
                    .forEach {
                        it.contact = contact
                        it.image//start downloading

                        send(it)
                    }*/
            }
        }
    }
}

object ForbiddenKeyWords : List<String> by listOf(
    "miku",
    "vocaloid",
    "kuriyama",
    "mirai"
) {
    override fun contains(element: String): Boolean {
        return this.stream().anyMatch { element.contains(it, ignoreCase = true) }
    }
}