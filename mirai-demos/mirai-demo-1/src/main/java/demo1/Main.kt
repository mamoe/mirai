@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package demo1

import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.subscribeAll
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeUntilFalse
import net.mamoe.mirai.login
import net.mamoe.mirai.message.Image
import net.mamoe.mirai.message.ImageId
import net.mamoe.mirai.message.PlainText
import net.mamoe.mirai.network.protocol.tim.packet.ClientRawPacket
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginResult
import net.mamoe.mirai.network.protocol.tim.packet.uploadImage
import net.mamoe.mirai.network.session
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.utils.*
import java.io.File
import javax.imageio.ImageIO

private fun readTestAccount(): BotAccount? {
    val file = File("testAccount.txt")
    if (!file.exists() || !file.canRead()) {
        return null
    }

    val lines = file.readLines()
    return try {
        BotAccount(lines[0].toUInt(), lines[1])
    } catch (e: IndexOutOfBoundsException) {
        null
    }
}

@Suppress("UNUSED_VARIABLE")
suspend fun main() {
    val bot = Bot(readTestAccount() ?: BotAccount(//填写你的账号
            account = 1994701121u,
            password = "123456"
    ), PlatformLogger())

    bot.login {
        randomDeviceName = true
    }.let {
        if (it != LoginResult.SUCCESS) {
            MiraiLogger.logError("Login failed: " + it.name)
            bot.close()
        }
    }

    //提供泛型以监听事件
    subscribeAlways<FriendMessageEvent> {
        //获取第一个纯文本消息, 获取不到会抛出 NoSuchElementException
        //val firstText = it.message.first<PlainText>()
        val firstText = it.message.firstOrNull<PlainText>()

        //获取第一个图片
        val firstImage = it.message.firstOrNull<Image>()

        when {
            it.message eq "你好" -> it.reply("你好!")

            "复读" in it.message -> it.sender.sendMessage(it.message)

            "发群" in it.message -> Group(bot, 580266363u).sendMessage("h")

            "直接发送包" in it.message -> {
                val d = ("01 " + 1994701021u.toByteArray().toUHexString() + " 3E 03 3F A2 00 00 02 BB 00 0A 00 01 00 01 00 5E 4F 53 52 6F 6F 74 3A 43 3A 5C 55 73 65 72 73 5C 48 69 6D 31 38 5C 44 6F 63 75 6D 65 6E 74 73 5C 54 65 6E 63 65 6E 74 20 46 69 6C 65 73 5C 31 30 34 30 34 30 30 32 39 30 5C 49 6D 61 67 65 5C 43 32 43 5C 7B 47 47 42 7E 49 31 5A 4D 43 28 25 49 4D 5A 5F 47 55 51 36 35 5D 51 2E 6A 70 67 00 00 04 7D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 35 02")
                        .hexToBytes()
                it.bot.network.socket.sendPacket(ClientRawPacket(0x01_BDu, it.bot.qqAccount, "00 00 00 01 2E 01 00 00 69 35".hexToBytes(), it.bot.network.session.sessionKey, d))
            }

            "上传好友图片" in it.message -> withTimeoutOrNull(3000) {
                val id = QQ(bot, 1040400290u).uploadImage(ImageIO.read(File("C:\\Users\\Him18\\Desktop\\lemon.png").readBytes().inputStream()))
                it.reply(id.value)
                delay(1000)
                it.reply(Image(id))
            }

            /*it.event eq "发图片群" -> sendGroupMessage(Group(session.bot, 580266363), PlainText("test") + UnsolvedImage(File("C:\\Users\\Him18\\Desktop\\faceImage_1559564477775.jpg")).also { image ->
                    image.upload(session, Group(session.bot, 580266363)).of()
                })*/

            it.message eq "发图片群2" -> Group(bot, 580266363u).sendMessage(Image(ImageId("{7AA4B3AA-8C3C-0F45-2D9B-7F302A0ACEAA}.jpg")))

            /* it.event eq "发图片" -> sendFriendMessage(it.sender, PlainText("test") + UnsolvedImage(File("C:\\Users\\Him18\\Desktop\\faceImage_1559564477775.jpg")).also { image ->
                     image.upload(session, it.sender).of()
                 })*/
            it.message eq "发图片2" -> it.reply(PlainText("test") + Image(ImageId("{7AA4B3AA-8C3C-0F45-2D9B-7F302A0ACEAA}.jpg")))
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
            val firstText = it.message.firstOrNull<PlainText>()

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