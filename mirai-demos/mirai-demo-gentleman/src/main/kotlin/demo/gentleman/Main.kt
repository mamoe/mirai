@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package demo.gentleman

import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotAccount
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
    } catch (e: IndexOutOfBoundsException) {
        null
    }
}

@Suppress("UNUSED_VARIABLE")
suspend fun main() {
    val bot = Bot(
        //readTestAccount() ?: BotAccount(
        qq = 1994701121u,
            password = "123456"
        // )
    )

    val bot2 = Bot(1994701121u, "").apply { login().requireSuccess() }

    bot.login().requireSuccess()

    bot.subscribeMessages {
        contains("") {

        }
    }

    bot.network.awaitDisconnection()//等到直到断开连接
}