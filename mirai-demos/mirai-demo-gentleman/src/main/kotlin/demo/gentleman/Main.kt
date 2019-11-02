@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package demo.gentleman

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.login
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

    /**
     * 监听所有事件
     */
    subscribeAlways<Event> {
        //bot.logger.verbose("收到了一个事件: ${it::class.simpleName}")
    }

    bot.subscribeMessages {
        "你好" reply "你好!"

        /*
        has<Image> {
            reply(message)
        }*/

        startsWith("随机图片", removePrefix = true) {
            withContext(Dispatchers.Default) {
                try {
                    repeat(it.toIntOrNull() ?: 1) {
                        launch {
                            Gentlemen.provide(subject).receive().image.await().send()
                        }
                    }
                } catch (e: Exception) {
                    reply(e.message ?: "exception: null")
                }
            }
        }

    }

    bot.network.awaitDisconnection()//等到直到断开连接
}