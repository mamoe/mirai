/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.imageplugin

import com.alibaba.fastjson.JSON
import kotlinx.coroutines.*
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.uploadAsImage
import org.jsoup.Jsoup

class ImageProvider {
    lateinit var contact: Contact

    // `Deferred<Image?>`  causes a runtime ClassCastException

    val image: Deferred<Image> by lazy {
        GlobalScope.async {
            withTimeoutOrNull(5 * 1000) {
                withContext(Dispatchers.IO) {
                    val result = JSON.parseArray(
                        Jsoup.connect("https://yande.re/post.json?limit=1&page=${(Math.random() * 10000).toInt()}").ignoreContentType(
                            true
                        ).timeout(
                            10_0000
                        ).get().body().text()
                    )
                    Jsoup.connect(result.getJSONObject(0).getString("jpeg_url"))
                        .userAgent("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_7; ja-jp) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/5.0.4 Safari/533.20.27")
                        .timeout(10_0000)
                        .ignoreContentType(true)
                        .maxBodySize(Int.MAX_VALUE)
                        .execute()
                        .bodyStream()
                }
            }?.uploadAsImage(contact) ?: error("Unable to download image|连接这个图站需要你的网络在外网")
        }
    }

}

