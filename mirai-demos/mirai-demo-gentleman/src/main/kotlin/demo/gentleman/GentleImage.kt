package demo.gentleman

import kotlinx.coroutines.*
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.Image
import net.mamoe.mirai.message.upload
import net.mamoe.mirai.utils.MiraiLogger
import org.jsoup.Jsoup

class GentleImage {
    lateinit var tags: String
    lateinit var sample_url: String
    lateinit var author: String
    lateinit var file_url: String

    var score: Int = 0

    var width: Int = 0
    var height: Int = 0

    //val summary by lazy { "Avatar by ${author}; Origin size ($width*$height);" + "HD URL: $file_url" }

    val name: String by lazy {
        var name: String
        val tags = tags.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (tags.isEmpty()) {
            return@lazy "OneTapper"
        }
        name = tags[(Math.random() * tags.size).toInt()]
        name = name.substring(0, 1).toUpperCase() + name.substring(1)
        name = name.split("\\(".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        name = name.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]

        name
    }


    lateinit var contact: Contact
    val image: Deferred<Image> by lazy {
        GlobalScope.async {
            // runBlocking {

            // CompletableDeferred(suspend {
            delay((Math.random() * 5000L).toLong())
            MiraiLogger.logPurple("Downloading image: $name")
            withContext(Dispatchers.IO) {
                Jsoup.connect(sample_url)
                    .userAgent(UserAgent.randomUserAgent)
                    .timeout(20_0000)
                    .ignoreContentType(true)
                    .maxBodySize(Int.MAX_VALUE)
                    .execute()
                    .bodyStream()
            }.upload(contact).also {
                MiraiLogger.logPurple("Downloaded image: $name")
            }
            // }())
            // }
        }
    }
}

