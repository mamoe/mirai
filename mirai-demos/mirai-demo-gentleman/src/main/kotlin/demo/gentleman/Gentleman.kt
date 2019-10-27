package demo.gentleman

import com.alibaba.fastjson.JSONArray
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import net.mamoe.mirai.contact.Contact
import org.jsoup.Connection
import org.jsoup.Jsoup


/**
 * 最少缓存的图片数量
 */
private const val IMAGE_BUFFER_CAPACITY: Int = 5

/**
 * 每次补充的数量
 */
private const val FILL_COUNT: Int = IMAGE_BUFFER_CAPACITY

@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
object Gentlemen : MutableMap<UInt, Gentleman> by mutableMapOf() {
    fun getOrPut(key: Contact): Gentleman = this.getOrPut(key.id) { Gentleman(key) }
}

@ExperimentalCoroutinesApi
class Gentleman(private val contact: Contact) : Channel<GentleImage> by Channel(IMAGE_BUFFER_CAPACITY) {
    init {

        GlobalScope.launch {
            while (!isClosedForSend) {
                val response = withContext(Dispatchers.IO) {
                    tryNTimes(2) {
                        Jsoup.connect("https://yande.re/post.json?")
                            .userAgent(UserAgent.randomUserAgent)
                            .data("limit", "20")
                            .data("page", (Math.random() * 4000).toString())
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
                    }
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