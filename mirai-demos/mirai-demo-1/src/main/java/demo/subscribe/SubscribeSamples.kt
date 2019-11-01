@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package demo.subscribe

import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.login
import net.mamoe.mirai.message.*
import net.mamoe.mirai.network.protocol.tim.packet.OutgoingRawPacket
import net.mamoe.mirai.network.protocol.tim.packet.PacketId
import net.mamoe.mirai.network.protocol.tim.packet.action.uploadImage
import net.mamoe.mirai.network.protocol.tim.packet.login.ifFail
import net.mamoe.mirai.network.session
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.utils.io.hexToBytes
import net.mamoe.mirai.utils.io.toByteArray
import net.mamoe.mirai.utils.io.toUHexString
import net.mamoe.mirai.utils.suspendToExternalImage
import java.io.File
import kotlin.system.exitProcess

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
    val bot = Bot(
        readTestAccount() ?: BotAccount(//填写你的账号
            id = 1994701121u,
            password = "123456"
        )
    )

    // 覆盖默认的配置
    bot.login {
        randomDeviceName = false
    }.ifFail {
        bot.logger.error("Login failed: $it")
        exitProcess(1)
    }

    bot.messageDSL()
    directlySubscribe(bot)

    //DSL 监听
    subscribeAll<FriendMessageEvent> {
        always {
            //获取第一个纯文本消息
            val firstText = it.message.firstOrNull<PlainText>()

        }
    }

    demo2()

    bot.network.awaitDisconnection()//等到直到断开连接
}

/**
 * 使用 dsl 监听消息事件
 *
 * @see subscribeFriendMessages
 * @see subscribeMessages
 * @see subscribeGroupMessages
 *
 * @see MessageSubscribersBuilder
 */
suspend fun Bot.messageDSL() {
    // 监听这个 bot 的来自所有群和好友的消息
    this.subscribeMessages {
        // 当接收到消息 == "你好" 时就回复 "你好!"
        "你好" reply "你好!"

        // 当消息 == "查看 subject" 时, 执行 lambda 并回复 lambda 的返回值
        case("查看 subject") {
            if (subject is QQ) {
                reply("消息主体为 QQ, 你在跟发私聊消息")
            } else {
                reply("消息主体为 Group, 你在群里发消息")
            }

            // 在回复的时候, 一般使用 subject 来作为回复对象.
            // 因为当群消息时, subject 为这个群.
            // 当好友消息时, subject 为这个好友.
            // 所有在 SenderAndMessage(也就是此时的 this 指代的对象) 中实现的扩展方法, 如刚刚的 "reply", 都是以 subject 作为目标
        }


        // 当消息里面包含这个类型的消息时
        has<Image> {
            // this: SenderAndMessage
            // message: MessageChain
            // sender: QQ
            // it: String (MessageChain.toString)

            if (this is GroupSenderAndMessage) {
                //如果是群消息
                // group: Group
                this.group.sendMessage("你在一个群里")
            }

            reply("图片, ID= ${message[Image].id}")//获取第一个 Image 类型的消息
            reply(message)
        }


        "123" containsReply "你的消息里面包含 123"


        // 当收到 "我的qq" 就执行 lambda 并回复 lambda 的返回值 String
        "我的qq" reply { sender.id.toString() }


        // 如果是这个 QQ 号发送的消息(可以是好友消息也可以是群消息)
        sentBy(1040400290) {
        }


        // 当消息前缀为 "我是" 时
        startsWith("我是", removePrefix = true) {
            // it: 删除了消息前缀 "我是" 后的消息
            // 如一条消息为 "我是张三", 则此时的 it 为 "张三".

            reply("你是$it")
        }


        // 当消息中包含 "复读" 时
        contains("复读") {
            reply(message)
        }


        // 自定义的 filter, filter 中 it 为转为 String 的消息.
        // 也可以用任何能在处理时使用的变量, 如 subject, sender, message
        content({ it.length == 3 }) {
            reply("你发送了长度为 3 的消息")
        }


        case("上传好友图片") {
            val filename = it.substringAfter("上传好友图片")
            File("C:\\Users\\Him18\\Desktop\\$filename").sendAsImageTo(subject)
        }

        case("上传群图片") {
            val filename = it.substringAfter("上传好友图片")
            File("C:\\Users\\Him18\\Desktop\\$filename").sendAsImageTo(subject)
        }
    }

    subscribeMessages {
        case("你好") {
            // this: SenderAndMessage
            // message: MessageChain
            // sender: QQ
            // it: String (来自 MessageChain.toString)
            // group: Group (如果是群消息)
            reply("你好")
        }
    }

    subscribeFriendMessages {
        contains("A") {
            // this: FriendSenderAndMessage
            // message: MessageChain
            // sender: QQ
            // it: String (来自 MessageChain.toString)
            reply("B")
        }
    }

    subscribeGroupMessages {
        // this: FriendSenderAndMessage
        // message: MessageChain
        // sender: QQ
        // it: String (来自 MessageChain.toString)
        // group: Group
    }
}

/**
 * 监听单个事件
 */
@Suppress("UNUSED_VARIABLE")
suspend fun directlySubscribe(bot: Bot) {
    // 手动处理消息
    // 使用 Bot 的扩展方法监听, 将在处理事件时得到一个 this: Bot.
    // 这样可以调用 Bot 内的一些扩展方法如 UInt.qq():QQ
    bot.subscribeAlways<FriendMessageEvent> {
        // this: Bot
        // it: FriendMessageEvent

        // 获取第一个纯文本消息, 获取不到会抛出 NoSuchElementException
        // val firstText = it.message.first<PlainText>()

        val firstText = it.message.firstOrNull<PlainText>()

        // 获取第一个图片
        val firstImage = it.message.firstOrNull<Image>()

        when {
            it.message eq "你好" -> it.reply("你好!")

            "复读" in it.message -> it.sender.sendMessage(it.message)

            "发群消息" in it.message -> 580266363u.group().sendMessage(it.message.toString().substringAfter("发群消息"))

            "直接发送包" in it.message -> {
                val d =
                    ("01 " + 1994701021u.toByteArray().toUHexString() + " 3E 03 3F A2 00 00 02 BB 00 0A 00 01 00 01 00 5E 4F 53 52 6F 6F 74 3A 43 3A 5C 55 73 65 72 73 5C 48 69 6D 31 38 5C 44 6F 63 75 6D 65 6E 74 73 5C 54 65 6E 63 65 6E 74 20 46 69 6C 65 73 5C 31 30 34 30 34 30 30 32 39 30 5C 49 6D 61 67 65 5C 43 32 43 5C 7B 47 47 42 7E 49 31 5A 4D 43 28 25 49 4D 5A 5F 47 55 51 36 35 5D 51 2E 6A 70 67 00 00 04 7D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 35 02")
                        .hexToBytes()
                it.bot.network.socket.sendPacket(
                    OutgoingRawPacket(
                        PacketId(0x01_BDu),
                        it.bot.qqAccount,
                        "00 00 00 01 2E 01 00 00 69 35".hexToBytes(),
                        it.bot.network.session.sessionKey,
                        d
                    )
                )
            }

            "上传群图片" in it.message -> withTimeoutOrNull(5000) {
                val filename = it.message.toString().substringAfter("上传群图片")
                val image = File(
                    "C:\\Users\\Him18\\Desktop\\$filename"
                ).suspendToExternalImage()
                920503456u.group().uploadImage(image)
                it.reply(image.groupImageId.value)
                delay(100)
                920503456u.group().sendMessage(Image(image.groupImageId))
            }

            "发群图片" in it.message -> {
                920503456u.group().sendMessage(Image(ImageId(it.message.toString().substringAfter("发群图片"))))
            }

            "发好友图片" in it.message -> {
                it.reply(Image(ImageId(it.message.toString().substringAfter("发好友图片"))))
            }

            /*it.event eq "发图片群" -> sendGroupMessage(Group(session.bot, 580266363), PlainText("test") + UnsolvedImage(File("C:\\Users\\Him18\\Desktop\\faceImage_1559564477775.jpg")).also { image ->
                    image.upload(session, Group(session.bot, 580266363)).of()
                })*/

            it.message eq "发图片群2" -> 580266363u.group().sendMessage(Image(ImageId("{7AA4B3AA-8C3C-0F45-2D9B-7F302A0ACEAA}.jpg")))

            /* it.event eq "发图片" -> sendFriendMessage(it.sentBy, PlainText("test") + UnsolvedImage(File("C:\\Users\\Him18\\Desktop\\faceImage_1559564477775.jpg")).also { image ->
                     image.upload(session, it.sentBy).of()
                 })*/
            it.message eq "发图片2" -> it.reply(PlainText("test") + Image(ImageId("{7AA4B3AA-8C3C-0F45-2D9B-7F302A0ACEAA}.jpg")))
            else -> {

            }
        }
    }
}

/**
 * 实现功能:
 * 对机器人说 "记笔记", 机器人记录之后的所有消息.
 * 对机器人说 "停止", 机器人停止
 */
suspend fun demo2() {
    subscribeAlways<FriendMessageEvent> { event ->
        if (event.message eq "记笔记") {
            subscribeUntilFalse<FriendMessageEvent> {
                it.reply("你发送了 ${it.message}")

                it.message eq "停止"
            }
        }
    }
}