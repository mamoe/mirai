package demo.gentleman

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import kotlinx.coroutines.*
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.uploadAsImage
import org.jsoup.Jsoup
import kotlin.random.Random

class GentleImage(val contact: Contact, val keyword: String) {

    val image: Deferred<Image> by lazy { getImage(0) }

    val seImage: Deferred<Image> by lazy { getImage(1) }

    fun getImage(r18: Int): Deferred<Image> {
        return GlobalScope.async {
            withTimeoutOrNull(10 * 1000) {
                withContext(Dispatchers.IO) {
                    val result =
                        JSON.parseObject(
                            Jsoup.connect("https://api.lolicon.app/setu/?r18=$r18" + if (keyword.isNotBlank()) "&keyword=$keyword&num=100" else "").ignoreContentType(
                                true
                            ).timeout(
                                10_0000
                            ).get().body().text()
                        )

                    val url: String
                    val pid: String
                    val data = result.getJSONArray("data")
                    with(JSONObject(data.getJSONObject(Random.nextInt(0, data.size)))) {
                        url = this.getString("url")
                        pid = this.getString("pid")
                    }

                    Jsoup
                        .connect(url)
                        .followRedirects(true)
                        .timeout(180_000)
                        .ignoreContentType(true)
                        .userAgent("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_7; ja-jp) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/5.0.4 Safari/533.20.27")
                        .referrer("https://www.pixiv.net/member_illust.php?mode=medium&illust_id=$pid")
                        .ignoreHttpErrors(true)
                        .maxBodySize(10000000)
                        .execute().also { check(it.statusCode() == 200) { "Failed to download image" } }
                }
            }?.bodyStream()?.uploadAsImage(contact) ?: error("Unable to download image")
        }
    }
}

