package demo.gentleman

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Contact


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