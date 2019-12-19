package demo.gentleman

import com.alibaba.fastjson.JSON
import kotlinx.coroutines.*
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.uploadAsImage
import org.jsoup.Jsoup

class GentleImage {
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

