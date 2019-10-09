package demo1

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.subscribeAll
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeUntilFalse
import net.mamoe.mirai.login
import net.mamoe.mirai.message.defaults.Image
import net.mamoe.mirai.message.defaults.PlainText
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginState
import net.mamoe.mirai.utils.BotAccount
import net.mamoe.mirai.utils.Console
import net.mamoe.mirai.utils.MiraiLogger
import kotlin.system.exitProcess

/**
 * @author Him188moe
 */
suspend fun main() {
    val bot = Bot(BotAccount(//填写你的账号
            qqNumber = 1994701121,
            password = "xiaoqqq"
    ), Console())

    bot.login().let {
        if(it != LoginState.SUCCESS) {
            MiraiLogger.error("Login failed: " + it.name)
            exitProcess(0)
        }
    }


    //DSL 监听
    FriendMessageEvent.subscribeAll {
        always {
            //获取第一个纯文本消息
            val firstText = it.message[PlainText]

        }
    }


    //监听事件:
    FriendMessageEvent.subscribeAlways {
        //获取第一个纯文本消息
        val firstText = it.message[PlainText]

        //获取第一个图片
        val firstImage = it.message[Image]

        when {
            it.message eq "你好" -> it.reply("你好!")

            "复读" in it.message -> it.sender.sendMessage(it.message)

            "发群" in it.message -> {
                it.message.list.toMutableList().let { messages ->
                    messages.removeAt(0)
                    Group(bot, 580266363).sendMessage(messages)
                }
            }

            /*it.message eq "发图片群" -> sendGroupMessage(Group(session.bot, 580266363), PlainText("test") + UnsolvedImage(File("C:\\Users\\Him18\\Desktop\\faceImage_1559564477775.jpg")).also { image ->
                    image.upload(session, Group(session.bot, 580266363)).of()
                })*/

            it.message eq "发图片群2" -> Group(bot, 580266363).sendMessage(Image("{7AA4B3AA-8C3C-0F45-2D9B-7F302A0ACEAA}.jpg"))

            /* it.message eq "发图片" -> sendFriendMessage(it.sender, PlainText("test") + UnsolvedImage(File("C:\\Users\\Him18\\Desktop\\faceImage_1559564477775.jpg")).also { image ->
                     image.upload(session, it.sender).of()
                 })*/
            it.message eq "发图片2" -> it.reply(PlainText("test") + Image("{7AA4B3AA-8C3C-0F45-2D9B-7F302A0ACEAA}.jpg"))
        }
    }

    GroupMessageEvent::class.subscribeAlways {
        when {
            it.message.contains("复读") -> it.reply(it.message)
        }
    }
}


/**
 * 实现功能:
 * 对机器人说 "记笔记", 机器人记录之后的所有消息.
 * 对机器人说 "停止", 机器人停止
 */
fun demo2() {
    FriendMessageEvent.subscribeAlways { event ->
        if (event.message eq "记笔记") {
            FriendMessageEvent.subscribeUntilFalse {
                it.reply("你发送了 ${it.message}")

                it.message eq "停止"
            }
        }
    }
}