import kotlinx.coroutines.GlobalScope
import net.mamoe.mirai.event.events.BotLoginSucceedEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.plugin.PluginBase
import net.mamoe.mirai.utils.MiraiExperimentalAPI

class ImageSenderMain : PluginBase() {

    @MiraiExperimentalAPI
    override fun onEnable() {
        logger.info("Image Sender plugin enabled")
        GlobalScope.subscribeAlways<BotLoginSucceedEvent> {
            logger.info("${this.bot.uin} login succeed, it will be controlled by Image Sender Plugin")
            this.bot.subscribeMessages {

                case("at me") {
                    (At(sender) + " ? ").reply()
                }

                (contains("image") || contains("图")) {
                    (At(sender) + " ? ").reply()
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