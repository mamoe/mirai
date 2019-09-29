package demo1

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.group.GroupMessageEvent
import net.mamoe.mirai.event.events.qq.FriendMessageEvent
import net.mamoe.mirai.event.hookAlways
import net.mamoe.mirai.message.defaults.Image
import net.mamoe.mirai.message.defaults.PlainText
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginState
import net.mamoe.mirai.utils.BotAccount
import net.mamoe.mirai.utils.Console

/**
 * @author Him188moe
 */
fun main() {
    val bot = Bot(BotAccount(
            qqNumber = 1683921395,
            password = "bb22222"
    ), Console())

    bot.network.tryLogin().get().let {
        check(it == LoginState.SUCCESS) { "Login failed: " + it.name }
    }

    //监听事件:
    FriendMessageEvent::class.hookAlways {
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

    GroupMessageEvent::class.hookAlways {
        when {
            it.message.contains("复读") -> it.reply(it.message)
        }
    }
}