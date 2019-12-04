@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package demo.gentleman

import com.soywiz.klock.months
import com.soywiz.klock.seconds
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mamoe.mirai.*
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.mute
import net.mamoe.mirai.event.Subscribable
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.*
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

    bot.subscribeGroupMessages {
        "群资料" reply {
            group.updateGroupInfo().toString().reply()
        }

        startsWith("mt2months") {
            val at: At by message
            at.target.member().mute(1.months)
        }

        startsWith("mute") {
            val at: At by message
            at.target.member().mute(30.seconds)
        }

        startsWith("unmute") {
            val at: At by message
            at.target.member().unmute()
        }
    }

    bot.subscribeMessages {
        case("at me") { At(sender).reply() }

        "你好" reply "你好!"

        startsWith("profile", removePrefix = true) {
            val account = it.trim()
            if (account.isNotEmpty()) {
                bot.getQQ(account.toUInt())
            } else {
                sender
            }.queryProfile().toString().reply()
        }

        "xml" reply {

            val template =
                """
        <?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
        <msg templateID='1' serviceID='1' action='plugin' actionData='ACTION_LINK' brief='BRIEF' flag='3' url=''>
            <item bg='0' layout='4'>
                <picture cover='TITLE_PICTURE_LINK'/>
                <title size='30' color='#fc7299'>TITLE</title>
            </item>
            <item>
                <summary color='#fc7299'>CONTENT</summary>
                <picture cover='CONTENT_PICTURE_LINK'/>
            </item>
            <source name='ExHentai' icon='ExHentai'/>
        </msg>
    """.trimIndent()

            buildXMLMessage {
                item {
                    picture("http://img.mamoe.net/2019/12/03/be35ccb489ecb.jpg")
                    title("This is title")
                }

                item {
                    summary("This is a summary colored #66CCFF", color = "#66CCFF")
                    picture("http://img.mamoe.net/2019/12/03/74c8614c4a161.jpg")
                }

                source("Mirai", "http://img.mamoe.net/2019/12/03/02eea0f6e826a.png")
            }.reply()
        }

        has<Image> {
            if (this is FriendMessage || (this is GroupMessage && this.permission == MemberPermission.ADMINISTRATOR)) {
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