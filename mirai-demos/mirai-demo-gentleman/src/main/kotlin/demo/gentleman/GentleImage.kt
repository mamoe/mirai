/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package demo.gentleman

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.uploadAsImage
import org.jsoup.Jsoup

class GentleImage(val contact: Contact, val keyword: String) {

    val image: Deferred<Image> by lazy { getImage(0) }

    val seImage: Deferred<Image> by lazy { getImage(1) }

    fun getImage(r18: Int): Deferred<Image> {
        return GlobalScope.async {
            withTimeoutOrNull(20 * 1000) {
                withContext(Dispatchers.IO) {

                    @Serializable
                    data class Setu(
                        val url: String,
                        val pid: String
                    )

                    @Serializable
                    data class Result(
                        val data: List<Setu>
                    )

                    val result =
                        Json.nonstrict.parse(
                            Result.serializer(),
                            Jsoup.connect("https://api.lolicon.app/setu/?r18=$r18" + if (keyword.isNotBlank()) "&keyword=$keyword&num=10" else "")
                                .ignoreContentType(true)
                                .userAgent(UserAgent.randomUserAgent)
                              //  .proxy("127.0.0.1", 1088)
                                .timeout(10_0000)
                                .get().body().text()
                        )

                    val setu = result.data.random()
                    Jsoup
                        .connect(setu.url)
                        .followRedirects(true)
                        .timeout(180_000)
                        .ignoreContentType(true)
                        .userAgent("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_7; ja-jp) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/5.0.4 Safari/533.20.27")
                        .referrer("https://www.pixiv.net/member_illust.php?mode=medium&illust_id=${setu.pid}")
                        //  .proxy("127.0.0.1", 1088)
                        .ignoreHttpErrors(true)
                        .maxBodySize(10000000)
                        .execute().also { check(it.statusCode() == 200) { "Failed to download image" } }
                }
            }?.bodyStream()?.uploadAsImage(contact) ?: error("Unable to download image")
        }
    }
}

