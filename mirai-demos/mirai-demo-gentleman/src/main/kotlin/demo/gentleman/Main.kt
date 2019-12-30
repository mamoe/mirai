@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package demo.gentleman

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.Subscribable
import net.mamoe.mirai.event.events.ReceiveFriendAddRequestEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.FriendMessage
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.buildXMLMessage
import net.mamoe.mirai.message.data.getValue
import net.mamoe.mirai.message.sendAsImageTo
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
        BotAccount(lines[0].toLong(), lines[1])
    } catch (e: Throwable) {
        null
    }
}

@Suppress("UNUSED_VARIABLE")
suspend fun main() {
    val bot = Bot(
        readTestAccount() ?: BotAccount(
            id = 913366033,
            passwordPlainText = "a18260132383"
        )
    ) {
        // override config here.
    }.alsoLogin()

    // 任何可以监听的对象都继承 Subscribable, 因此这个订阅会订阅全部的事件.
    GlobalScope.subscribeAlways<Subscribable> {
        //bot.logger.verbose("收到了一个事件: $this")
    }

    // 全局范围订阅事件, 不受 bot 实例影响
    GlobalScope.subscribeAlways<ReceiveFriendAddRequestEvent> {
        it.approve()
    }

    // 订阅来自这个 bot 的群消息事件
    bot.subscribeGroupMessages {
        "群资料" reply {
            group.updateGroupInfo().toString().reply()
        }

        startsWith("mute") {
            val at: At by message
            at.member().mute(30)
        }

        startsWith("unmute") {
            val at: At by message
            at.member().unmute()
        }
    }

    // 订阅来自这个 bot 的消息事件, 可以是群消息也可以是好友消息
    bot.subscribeMessages {
        always {
        }

        case("at me") { At(sender).reply() }
        // 等同于  "at me" reply { At(sender) }

        "你好" reply "你好!"

        startsWith("profile", removePrefix = true) {
            val account = it.trim()
            if (account.isNotEmpty()) {
                account.toLong().qq()
            } else {
                sender
            }.queryProfile().toString().reply()
        }
        "grouplist" reply {

            //"https://ssl.ptlogin2.qq.com/jump?pt_clientver=5509&pt_src=1&keyindex=9&clientuin=" + bot.qqAccount + "&clientkey=" + com.tick_tock.pctim.utils.Util.byte2HexString(
            //    user.txprotocol.serviceTicketHttp
            //).replace(" ", "").toString() + "&u1=http%3A%2F%2Fqun.qq.com%2Fmember.html%23gid%3D168209441"
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
            if (this is FriendMessage || (this is GroupMessage && this.permission == MemberPermission.ADMINISTRATOR)) withContext(IO) {
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
            reply(bot.addFriend(it.toLong()).toString())
        }

    }

    bot.network.awaitDisconnection()//等到直到断开连接
}

private fun newTestTempFile(filename: String = "${UUID.randomUUID()}", suffix: String = ".tmp"): File =
    File(System.getProperty("user.dir"), filename + suffix).also { it.createNewFile(); it.deleteOnExit() }