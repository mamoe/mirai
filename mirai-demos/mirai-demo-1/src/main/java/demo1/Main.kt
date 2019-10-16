package demo1

import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.subscribeAll
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeOnce
import net.mamoe.mirai.event.subscribeUntilFalse
import net.mamoe.mirai.login
import net.mamoe.mirai.message.Image
import net.mamoe.mirai.message.PlainText
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginResult
import net.mamoe.mirai.utils.BotAccount
import net.mamoe.mirai.utils.Console
import net.mamoe.mirai.utils.MiraiLogger
import kotlin.coroutines.coroutineContext
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

suspend fun main() {
    val bot = Bot(BotAccount(//填写你的账号
            qqNumber = 2903772581,
            password = "zxc123456"
    ), Console())

    bot.login {
        touchTimeoutMillis = 2000
        randomDeviceName = true
    }.let {
        if (it != LoginResult.SUCCESS) {
            MiraiLogger.logError("Login failed: " + it.name)
            bot.close()
        }
    }

    //提供泛型以监听事件
    subscribeOnce<FriendMessageEvent> {
        //获取第一个纯文本消息, 获取不到会抛出 NoSuchElementException
        val firstText = it.message.first<PlainText>()

        //获取第一个图片
        val firstImage = it.message.firstOrNull<Image>()

        when {
            it.message eq "你好" -> it.reply("你好!")

            "复读" in it.message -> it.sender.sendMessage(it.message)

            "发群" in it.message -> {

            }

            /*it.event eq "发图片群" -> sendGroupMessage(Group(session.bot, 580266363), PlainText("test") + UnsolvedImage(File("C:\\Users\\Him18\\Desktop\\faceImage_1559564477775.jpg")).also { image ->
                    image.upload(session, Group(session.bot, 580266363)).of()
                })*/

            it.message eq "发图片群2" -> Group(bot, 580266363).sendMessage(Image("{7AA4B3AA-8C3C-0F45-2D9B-7F302A0ACEAA}.jpg"))

            /* it.event eq "发图片" -> sendFriendMessage(it.sender, PlainText("test") + UnsolvedImage(File("C:\\Users\\Him18\\Desktop\\faceImage_1559564477775.jpg")).also { image ->
                     image.upload(session, it.sender).of()
                 })*/
            it.message eq "发图片2" -> it.reply(PlainText("test") + Image("{7AA4B3AA-8C3C-0F45-2D9B-7F302A0ACEAA}.jpg"))
        }
    }

    //通过 KClass 扩展方式监听事件(不推荐)
    GroupMessageEvent::class.subscribeAlways {
        when {
            it.message.contains("复读") -> it.reply(it.message)
        }
    }


    //DSL 监听
    subscribeAll<FriendMessageEvent> {
        always {
            //获取第一个纯文本消息
            val firstText = it.message.first<PlainText>()

        }
    }

    demo2()

    //由于使用的是协程, main函数执行完后就会结束程序.
    delay(Long.MAX_VALUE)//永远等待, 以测试事件
}


/**
 * 实现功能:
 * 对机器人说 "记笔记", 机器人记录之后的所有消息.
 * 对机器人说 "停止", 机器人停止
 */
fun demo2() {
    subscribeAlways<FriendMessageEvent> { event ->
        if (event.message eq "记笔记") {
            subscribeUntilFalse<FriendMessageEvent> {
                it.reply("你发送了 ${it.message}")

                it.message eq "停止"
            }
        }
    }
}