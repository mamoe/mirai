package net.mamoe.mirai.imageplugin

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import net.mamoe.mirai.event.events.BotLoginSucceedEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.plugin.PluginBase
import net.mamoe.mirai.utils.MiraiExperimentalAPI

class ImageSenderMain : PluginBase() {
    @ExperimentalCoroutinesApi
    @MiraiExperimentalAPI
    override fun onEnable() {
        logger.info("Image Sender plugin enabled")
        GlobalScope.subscribeAlways<BotLoginSucceedEvent> {
            logger.info("${this.bot.uin} login succeed, it will be controlled by Image Sender Plugin")
            this.bot.subscribeMessages {

                case("at me") {
                    (At(sender) + " ? ").reply()
                }

                (contains("image") or contains("图")) {
                    "图片发送中".reply()
                    ImageProvider().apply {
                        this.contact = sender
                    }.image.await().reply()
                }

            }
        }
    }

    override fun onLoad() {
        logger.info("loading...")
    }

    override fun onDisable() {

    }
}