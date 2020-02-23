/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.imageplugin

import kotlinx.coroutines.*
import net.mamoe.mirai.console.plugins.Config
import net.mamoe.mirai.console.plugins.ConfigSection
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.uploadAsImage
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import org.jsoup.Jsoup
import java.io.File
import kotlin.random.Random

class ImageSenderMain : PluginBase() {

    lateinit var images: Config
    lateinit var normal: List<ConfigSection>
    lateinit var r18: List<ConfigSection>

    @ExperimentalCoroutinesApi
    @MiraiExperimentalAPI
    override fun onEnable() {
        logger.info("Image Sender plugin enabled")
        GlobalScope.subscribeAlways<BotOnlineEvent> {
            logger.info("${this.bot.uin} login succeed, it will be controlled by Image Sender Plugin")
            this.bot.subscribeMessages {
                (contains("色图")) {
                    try {
                        with(normal.random()) {
                            getImage(
                                subject, this.getString("url"), this.getString("pid")
                            ).plus(this.getString("tags")).send()
                        }
                    } catch (e: Exception) {
                        reply(e.message ?: "unknown error")
                    }
                }

                (contains("不够色")) {
                    try {
                        with(r18.random()) {
                            getImage(
                                subject, this.getString("url"), this.getString("pid")
                            ).plus(this.getString("tags")).send()
                        }
                    } catch (e: Exception) {
                        reply(e.message ?: "unknown error")
                    }
                }

            }
        }
    }

    suspend fun getImage(contact: Contact, url: String, pid: String): Image {
        return withTimeoutOrNull(20 * 1000) {
            withContext(Dispatchers.IO) {
                Jsoup
                    .connect(url)
                    .followRedirects(true)
                    .timeout(180_000)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_7; ja-jp) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/5.0.4 Safari/533.20.27")
                    .referrer("https://www.pixiv.net/member_illust.php?mode=medium&illust_id=$pid")
                    .ignoreHttpErrors(true)
                    .maxBodySize(100000000)
                    .execute().also { check(it.statusCode() == 200) { "Failed to download image" } }
            }
        }?.bodyStream()?.uploadAsImage(contact) ?: error("Unable to download image")
    }

    override fun onLoad() {
        logger.info("loading local image data")
        try {
            images = Config.load(this.javaClass.classLoader.getResource("data.yml")!!.path!!)
        } catch (e: Exception) {
            logger.info("无法加载本地图片")
        }
        logger.info("本地图片版本" + images.getString("version"))
        logger.info("Normal * " + images.getList("normal").size)
        logger.info("R18    * " + images.getList("R18").size)
    }

    override fun onDisable() {

    }
}