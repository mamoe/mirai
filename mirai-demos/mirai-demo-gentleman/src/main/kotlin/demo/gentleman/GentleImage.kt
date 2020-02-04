package demo.gentleman

import com.alibaba.fastjson.JSON
import kotlinx.coroutines.*
import kotlinx.serialization.json.JsonObject
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.uploadAsImage
import org.jsoup.Jsoup

class GentleImage {
    lateinit var contact: Contact

    // `Deferred<Image?>`  causes a runtime ClassCastException

    val image: Deferred<Image> by lazy { getImage(0) }

    val seImage: Deferred<Image> by lazy { getImage(1) }

    fun getImage(r18: Int): Deferred<Image> {
        GlobalScope.async {
            withTimeoutOrNull(5 * 1000) {
                withContext(Dispatchers.IO) {
                    val result = JSON.parseObject(
                        Jsoup.connect("https://api.lolicon.app/setu/?r18=$r18").ignoreContentType(true).timeout(10_0000).get().body().text(),
                        )

                    var url = "";
                    var pid = "";
                    with(result.getJSONArray("data").getJSONObject(0)) {
                        url = this.getString("url")
                        pid = this.getString("pid")
                    }

                    val image = Jsoup
                        .connect(url)
                        .followRedirects(true)
                        .timeout(180_000)
                        .ignoreContentType(true)
                        .userAgent("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_7; ja-jp) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/5.0.4 Safari/533.20.27")
                        .referrer("https://www.pixiv.net/member_illust.php?mode=medium&illust_id=$pid")
                        .ignoreHttpErrors(true)
                        .maxBodySize(10000000)
                        .execute()

                    if (image.statusCode() != 200) error("Failed to download image")
                }
                image.bodyStream().uploadAsImage(contact) ?: error("Unable to upload image")
            }
        }
    }
}

