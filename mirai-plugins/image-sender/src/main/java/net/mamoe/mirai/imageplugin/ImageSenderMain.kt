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
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.registerCommand
import net.mamoe.mirai.console.plugins.Config
import net.mamoe.mirai.console.plugins.ConfigSection
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.console.plugins.withDefaultWriteSave
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.sendMessage
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.MemberPermissionChangeEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.message.upload
import net.mamoe.mirai.message.uploadAsImage
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import org.jsoup.Jsoup
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.imageio.ImageIO


class ImageSenderMain : PluginBase() {

    lateinit var images: Config
    lateinit var normal: List<ConfigSection>
    lateinit var r18: List<ConfigSection>


    val config by lazy {
        loadConfig("setting.yml")
    }

    val Normal_Image_Trigger by config.withDefaultWriteSave { "色图" }
    val R18_Image_Trigger by config.withDefaultWriteSave { "不够色" }
    val Image_Resize_Max_Width_Height by config.withDefaultWriteSave { 800 }

    val groupsAllowNormal by lazy {
        config.getLongList("Allow_Normal_Image_Groups").toMutableList()
    }

    val groupsAllowR18 by lazy {
        config.getLongList("Allow_R18_Image_Groups").toMutableList()
    }

    override fun onDisable() {
        config["Allow_R18_Image_Groups"] = groupsAllowR18
        config["Allow_Normal_Image_Groups"] = groupsAllowNormal
        config.save()
    }

    override fun onEnable() {
        logger.info("Image Sender plugin enabled")
        registerCommands()
        subscribeAlways<MemberPermissionChangeEvent> {
            logger.info("${this.bot.uin} login succeed, it will be controlled by Image Sender Plugin")
            this.bot.subscribeGroupMessages {
                (contains(Normal_Image_Trigger)) {
                    sendImage(subject, normal.random())
                }
                (contains(R18_Image_Trigger)) {
                    sendImage(subject, r18.random())
                }
            }
        }
    }

    private fun sendImage(contact: Contact, configSection: ConfigSection) {
        launch {
            try {
                logger.info("正在推送图片")
                getImage(
                    contact, configSection.getString("url"), configSection.getString("pid"), 800
                ).plus(configSection.getString("tags")).sendTo(contact)
            } catch (e: Exception) {
                contact.sendMessage(e.message ?: "unknown error")
            }
        }
    }

    private suspend fun getImage(contact: Contact, url: String, pid: String, maxWidthOrHeight: Int): Image {
        val bodyStream = withTimeoutOrNull(20 * 1000) {
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
        }?.bodyStream() ?: error("Failed to download image")
        if (maxWidthOrHeight < 1) {
            return bodyStream.uploadAsImage(contact)
        }
        val image = withContext(Dispatchers.IO) {
            ImageIO.read(bodyStream)
        }
        if (image.width.coerceAtLeast(image.height) <= maxWidthOrHeight) {
            return image.upload(contact)
        }
        val rate = (maxWidthOrHeight.toFloat() / image.width.coerceAtLeast(image.height))
        val newWidth = (image.width * rate).toInt()
        val newHeight = (image.height * rate).toInt()
        return withContext(Dispatchers.IO) {
            val dimg = BufferedImage(newWidth, newHeight, image.type)
            val g = dimg.createGraphics()
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            g.drawImage(image, 0, 0, newWidth, newHeight, 0, 0, image.width, image.height, null)
            g.dispose()
            dimg
        }.upload(contact)
    }


    override fun onLoad() {
        logger.info("loading local image data")

        try {
            images = Config.load(getResources(fileName = "data.yml")!!, "yml")
        } catch (e: Exception) {
            e.printStackTrace()
            logger.info("无法加载本地图片")
        }
        logger.info("本地图片版本" + images.getString("version"))
        r18 = images.getConfigSectionList("R18")
        normal = images.getConfigSectionList("normal")
        logger.info("Normal * " + normal.size)
        logger.info("R18    * " + r18.size)
    }

    fun registerCommands() {
        registerCommand {

        }
    }

}