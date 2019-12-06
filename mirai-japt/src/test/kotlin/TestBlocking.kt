import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.japt.BlockingContacts
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginResult
import java.io.File

@ExperimentalUnsignedTypes
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

@ExperimentalUnsignedTypes
suspend fun main() {
    val bot = Bot(readTestAccount()!!)
    if (bot.network.login() != LoginResult.SUCCESS) {
        throw IllegalStateException("Login failed")
    }

    val createBlocking = BlockingContacts.createBlocking(bot.contacts.getQQ(123L))
    println(createBlocking.queryRemark())
}