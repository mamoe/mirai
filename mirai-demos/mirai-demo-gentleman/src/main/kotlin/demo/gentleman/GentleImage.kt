package demo.gentleman

import com.alibaba.fastjson.JSON
import kotlinx.coroutines.*
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.Image
import net.mamoe.mirai.message.uploadAsImage
import org.jsoup.Jsoup

class GentleImage {
    lateinit var tags: String
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
    // `Deferred<Image?>`  causes a runtime ClassCastException
    val image: Deferred<Image> by lazy {
        GlobalScope.async {
            //delay((Math.random() * 5000L).toLong())
            class Result {
                var id: String = ""
            }

            withTimeoutOrNull(5 * 1000) {
                withContext(Dispatchers.IO) {
                    val result = JSON.parseObject(
                        Jsoup.connect("http://dev.itxtech.org:10322/v2/randomImg.uue").ignoreContentType(true).timeout(10_0000).get().body().text(),
                        Result::class.java
                    )

                    Jsoup.connect("http://dev.itxtech.org:10322/img.uue?size=large&id=${result.id}")
                        .userAgent(UserAgent.randomUserAgent)
                        .timeout(10_0000)
                        .ignoreContentType(true)
                        .maxBodySize(Int.MAX_VALUE)
                        .execute()
                        .bodyStream()
                }
            }?.uploadAsImage(contact) ?: error("Unable to download image")
        }
    }

}

