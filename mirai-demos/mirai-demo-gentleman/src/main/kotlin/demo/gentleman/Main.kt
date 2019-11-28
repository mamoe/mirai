@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package demo.gentleman

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mamoe.mirai.*
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.Subscribable
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.At
import net.mamoe.mirai.message.Image
import net.mamoe.mirai.message.getValue
import net.mamoe.mirai.message.sendAsImageTo
import net.mamoe.mirai.network.protocol.tim.packet.event.FriendMessage
import net.mamoe.mirai.network.protocol.tim.packet.event.GroupMessage
import net.mamoe.mirai.network.protocol.tim.packet.event.ReceiveFriendAddRequestEvent
import java.io.File
import java.util.*
import javax.swing.filechooser.FileSystemView
import kotlin.random.Random

private fun readTestAccount(): BotAccount? {
    val file = File("testAccount.txt")
    if (!file.exists() || !file.canRead()) {
        return null
    }

    val lines = file.readLines()
    return try {
        BotAccount(lines[0].toUInt(), lines[1])
    } catch (e: Exception) {
        null
    }
}

@Suppress("UNUSED_VARIABLE")
suspend fun main() {
    val bot = Bot(
        readTestAccount() ?: BotAccount(
            id = 913366033u,
            password = "a18260132383"
        )
    ).alsoLogin()

    /**
     * 监听所有事件
     */
    subscribeAlways<Subscribable> {

        //bot.logger.verbose("收到了一个事件: ${it::class.simpleName}")
    }

    subscribeAlways<ReceiveFriendAddRequestEvent> {
        it.approve()
    }

    bot.subscribeMessages {
        case("at me") { At(sender).reply() }

        "你好" reply "你好!"

        "群资料" reply {
            if (this is GroupMessage) {
                group.updateGroupInfo().toString().reply()
            }
        }

        startsWith("profile", removePrefix = true) {
            val account = it.trim()
            if (account.isNotEmpty()) {
                bot.getQQ(account.toUInt())
            } else {
                sender
            }.queryProfile().toString().reply()
        }

        has<Image> {
            if (this is FriendMessage || (this is GroupMessage && this.permission == MemberPermission.OPERATOR)) {
                withContext(IO) {
                    val image: Image by message
                    // 等同于 val image = message[Image]

                    try {
                        image.downloadTo(newTestTempFile(suffix = ".png").also { reply("Temp file: ${it.absolutePath}") })
                        reply(image.id.value + " downloaded")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        reply(e.message ?: e::class.java.simpleName)
                    }
                }
            }
        }

        startsWith("上传图片", removePrefix = true) handler@{
            val file = File(FileSystemView.getFileSystemView().homeDirectory, it)
            if (!file.exists()) {
                reply("图片不存在")
                return@handler
            }

            reply("sent")
            file.sendAsImageTo(subject)
        }

        startsWith("随机图片", removePrefix = true) {
            repeat(it.toIntOrNull() ?: 1) {
                GlobalScope.launch {
                    delay(Random.Default.nextLong(100, 1000))
                    Gentlemen.provide(subject).receive().image.await().send()
                }
            }
        }

        startsWith("添加好友", removePrefix = true) {
            reply(bot.addFriend(it.toUInt()).toString())
        }

    }

    bot.network.awaitDisconnection()//等到直到断开连接
}

private fun newTestTempFile(filename: String = "${UUID.randomUUID()}", suffix: String = ".tmp"): File =
    File(System.getProperty("user.dir"), filename + suffix).also { it.createNewFile(); it.deleteOnExit() }