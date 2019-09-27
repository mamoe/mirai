import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.packet.login.LoginState
import net.mamoe.mirai.utils.BotAccount
import java.util.*

/**
 * 筛选掉无法登录(冻结/设备锁/UNKNOWN)的 qq
 *
 * @author Him188moe
 */

val qqList = "2535777366----abc123456\n" +
        "2535815148----abc123456\n" +
        "2535704896----abc123456\n" +
        "2535744882----abc123456\n" +
        "2535656918----abc123456\n" +
        "2535679286----abc123456\n" +
        "2535606374----abc123456\n" +
        "2535647743----abc123456\n" +
        "2535543049----abc123456\n" +
        "2535583893----abc123456\n" +
        "2535508338----abc123456\n" +
        "2535524178----abc123456\n" +
        "2535363077----abc123456\n" +
        "2535469090----abc123456\n" +
        "2535263758----abc123456\n" +
        "2535258328----abc123456\n" +
        "2535175332----abc123456\n" +
        "2535175855----abc123456\n" +
        "2535126490----abc123456\n" +
        "2535169081----abc123456\n" +
        "2535054551----abc123456\n" +
        "2535085068----abc123456\n" +
        "2535041182----abc123456\n" +
        "2535055583----abc123456\n" +
        "2534883752----abc123456\n" +
        "2534909231----abc123456\n" +
        "2534715278----abc123456\n" +
        "2534766467----abc123456\n" +
        "2534696956----abc123456\n" +
        "2534703892----abc123456\n" +
        "2534597961----abc123456\n" +
        "2534687923----abc123456\n" +
        "2534573690----abc123456\n" +
        "2534596747----abc123456\n" +
        "2534467863----abc123456\n" +
        "2534480141----abc123456\n" +
        "2534377951----abc123456\n" +
        "2534418547----abc123456\n" +
        "2534315990----abc123456\n" +
        "2534318348----abc123456\n" +
        "2534220616----abc123456\n" +
        "2534288430----abc123456\n" +
        "2534205633----abc123456\n" +
        "2534226589----abc123456\n" +
        "2534182470----abc123456\n" +
        "2534194558----abc123456\n" +
        "2534106061----abc123456\n" +
        "2534108283----abc123456\n" +
        "2534026460----abc123456\n" +
        "2534037598----abc123456\n"


fun main() {
    val goodBotList = Collections.synchronizedList(mutableListOf<Bot>())

    qqList.split("\n").forEach {
        GlobalScope.launch {
            val strings = it.split("----")
            val bot = Bot(BotAccount(strings[0].toLong(), strings[1].let { password ->
                if (password.endsWith(".")) {
                    return@let password.substring(0, password.length - 1)
                }
                return@let password
            }))

            bot.network.tryLogin().whenComplete { state, _ ->
                if (!(state == LoginState.BLOCKED || state == LoginState.DEVICE_LOCK || state == LoginState.WRONG_PASSWORD)) {
                    goodBotList.add(bot)
                }
            }
        }
    }

    Thread.sleep(9 * 3000)

    println(goodBotList.joinToString("\n") { it.account.qqNumber.toString() + "    " + it.account.password })
}
