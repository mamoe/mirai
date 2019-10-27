@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package demo.gentleman

import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.login
import net.mamoe.mirai.message.Image
import net.mamoe.mirai.message.ImageId
import net.mamoe.mirai.network.protocol.tim.packet.login.requireSuccess
import java.io.File

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
            id = 1994701121u,
            password = "123456"
        )
    ).apply { login().requireSuccess() }

    bot.subscribeMessages {
        "你好" reply "你好!"

        startsWith("发送图片", removePrefix = true) {
            reply(Image(ImageId(it)))
        }

        case("随机色图") {
            Gentlemen.getOrPut(subject).receive().image.await().send()
        }

        "色图" caseReply {

            ""
        }
    }

    bot.network.awaitDisconnection()//等到直到断开连接
}