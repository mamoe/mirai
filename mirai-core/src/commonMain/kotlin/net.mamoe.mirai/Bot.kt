@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai

import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.Bot.ContactSystem
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.TIMBotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.packet.action.AddFriendPacket
import net.mamoe.mirai.network.protocol.tim.packet.action.CanAddFriendPacket
import net.mamoe.mirai.network.protocol.tim.packet.action.CanAddFriendResponse
import net.mamoe.mirai.network.protocol.tim.packet.action.RequestFriendAdditionKeyPacket
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginResult
import net.mamoe.mirai.network.sessionKey
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.internal.coerceAtLeastOrFail
import kotlin.jvm.JvmOverloads

data class BotAccount(
    val id: UInt,
    val password: String//todo 不保存 password?
)

/**
 * Mirai 的机器人. 一个机器人实例登录一个 QQ 账号.
 * Mirai 为多账号设计, 可同时维护多个机器人.
 *
 * [Bot] 由 3 个模块组成.
 * [联系人管理][ContactSystem]: 可通过 [Bot.contacts] 访问
 * [网络处理器][TIMBotNetworkHandler]: 可通过 [Bot.network] 访问
 * [机器人账号信息][BotAccount]: 可通过 [Bot.qqAccount] 访问
 *
 * 若需要得到机器人的 QQ 账号, 请访问 [Bot.qqAccount]
 * 若需要得到服务器上所有机器人列表, 请访问 [Bot.instances]
 *
 * 在 BotHelper.kt 中有一些访问的捷径. 如 [Bot.getGroup]
 *
 *
 *
 * Bot that is the base of the whole program.
 * It consists of
 * a [ContactSystem], which manage contacts such as [QQ] and [Group];
 * a [TIMBotNetworkHandler], which manages the connection to the server;
 * a [BotAccount], which stores the account information(e.g. qq number the bot)
 *
 * To of all the QQ contacts, access [Bot.qqAccount]
 * To of all the Robot instance, access [Bot.instances]
 *
 *
 * @author Him188moe
 * @author NaturalHG
 * @see Contact
 */
class Bot(val account: BotAccount, val logger: MiraiLogger) {
    constructor(qq: UInt, password: String) : this(BotAccount(qq, password))
    constructor(account: BotAccount) : this(account, DefaultLogger("Bot(" + account.id + ")"))

    val contacts = ContactSystem()

    var network: BotNetworkHandler<*> = TIMBotNetworkHandler(this)

    init {
        instances.add(this)
    }

    override fun toString(): String = "Bot(${account.id})"

    /**
     * [关闭][BotNetworkHandler.close]网络处理器, 取消所有运行在 [BotNetworkHandler] 下的协程.
     * 然后重新启动并尝试登录
     */
    @JvmOverloads
    suspend fun reinitializeNetworkHandler(
        configuration: BotConfiguration,
        cause: Throwable? = null
    ): LoginResult {
        logger.info("Initializing BotNetworkHandler")
        try {
            network.close(cause)
        } catch (e: Exception) {
            logger.error(e)
        }
        network = TIMBotNetworkHandler(this)
        return network.login(configuration)
    }

    /**
     * Bot 联系人管理.
     *
     * @see Bot.contacts
     */
    inner class ContactSystem internal constructor() {
        inline val bot: Bot get() = this@Bot

        private val _groups = ContactList<Group>()
        private lateinit var groupsUpdater: Job
        val groups = ContactList<Group>()
        private val groupsLock = Mutex()

        private val _qqs = ContactList<QQ>() //todo 实现群列表和好友列表获取
        private lateinit var qqUpdaterJob: Job
        val qqs: ContactList<QQ> = _qqs
        private val qqsLock = Mutex()

        /**
         * 获取缓存的 QQ 对象. 若没有对应的缓存, 则会创建一个.
         *
         * 注: 这个方法是线程安全的
         */
        suspend fun getQQ(id: UInt): QQ =
            if (qqs.containsKey(id)) qqs[id]!!
            else qqsLock.withLock {
                qqs.getOrPut(id) { QQ(bot, id) }
            }

        /**
         * 获取缓存的群对象. 若没有对应的缓存, 则会创建一个.
         *
         * 注: 这个方法是线程安全的
         */
        suspend fun getGroup(internalId: GroupInternalId): Group = getGroup(internalId.toId())

        /**
         * 获取缓存的群对象. 若没有对应的缓存, 则会创建一个.
         *
         * 注: 这个方法是线程安全的
         */
        suspend fun getGroup(id: GroupId): Group = id.value.let {
            if (groups.containsKey(it)) groups[it]!!
            else groupsLock.withLock {
                groups.getOrPut(it) { Group(bot, id) }
            }
        }

    }

    suspend inline fun Int.qq(): QQ = getQQ(this.coerceAtLeastOrFail(0).toUInt())
    suspend inline fun Long.qq(): QQ = getQQ(this.coerceAtLeastOrFail(0))
    suspend inline fun UInt.qq(): QQ = getQQ(this)

    suspend inline fun Int.group(): Group = getGroup(this.coerceAtLeastOrFail(0).toUInt())
    suspend inline fun Long.group(): Group = getGroup(this.coerceAtLeastOrFail(0))
    suspend inline fun UInt.group(): Group = getGroup(GroupId(this))
    suspend inline fun GroupId.group(): Group = getGroup(this)
    suspend inline fun GroupInternalId.group(): Group = getGroup(this)

    suspend fun close() {
        this.network.close()
        this.contacts.groups.clear()
        this.contacts.qqs.clear()
    }

    companion object {
        val instances: MutableList<Bot> = mutableListOf()
    }
}

@Suppress("ClassName")
sealed class AddFriendResult {

    abstract class DONE internal constructor() : AddFriendResult() {
        override fun toString(): String = "AddFriendResult(Done)"
    }

    /**
     * 对方拒绝添加好友
     */
    object REJECTED : AddFriendResult() {
        override fun toString(): String = "AddFriendResult(Rejected)"
    }

    /**
     * 这个人已经是好友
     */
    object ALREADY_ADDED : DONE() {
        override fun toString(): String = "AddFriendResult(AlreadyAdded)"
    }

    /**
     * 等待对方同意
     */
    object WAITING_FOR_APPROVE : DONE() {
        override fun toString(): String = "AddFriendResult(WaitingForApprove)"
    }

    /**
     * 成功添加 (只在对方设置为允许任何人直接添加为好友时才会获得这个结果)
     */
    object ADDED : DONE() {
        override fun toString(): String = "AddFriendResult(Added)"
    }
}


/*

// TODO: 2019/11/11 其中一个是对方已同意添加好友的包

Mirai 22:04:48 : Packet received: UnknownEventPacket(id=00 BC, identity=(2092749761->1994701021))
 = 00 00 00 08 00 0A 00 04 01 00 00 00 01 00 00 0F 00 00 00 00 00 00 00 00 01 03 EB 00 02 0A 00
Mirai 22:04:48 : Packet received: UnknownEventPacket(id=00 D6, identity=(2092749761->1994701021))
 = 00 00 00 08 00 0A 00 04 01 00 00 00 00 00 00 00 BC 01 00 00 00 00 02 00 00
 */

/**
 * 添加一个好友
 *
 * @param lazyMessage 若需要验证请求时的验证消息.
 */
suspend fun ContactSystem.addFriend(id: UInt, lazyMessage: () -> String = { "" }, lazyRemark: () -> String = { "" }): AddFriendResult = bot.withSession {
    when (CanAddFriendPacket(bot.qqAccount, id, bot.sessionKey).sendAndExpect<CanAddFriendResponse>()) {
        is CanAddFriendResponse.AlreadyAdded -> AddFriendResult.ALREADY_ADDED
        is CanAddFriendResponse.Rejected -> AddFriendResult.REJECTED

        is CanAddFriendResponse.ReadyToAdd,
        is CanAddFriendResponse.RequireVerification -> {
            val key = RequestFriendAdditionKeyPacket(bot.qqAccount, id, sessionKey).sendAndExpect<RequestFriendAdditionKeyPacket.Response>().key
            AddFriendPacket(bot.qqAccount, id, sessionKey, lazyMessage(), lazyRemark(), key).sendAndExpect<AddFriendPacket.Response>()
            return AddFriendResult.WAITING_FOR_APPROVE
        }
        //这个做的是需要验证消息的情况, 不确定 ReadyToAdd 的是啥

        // 似乎 RequireVerification 和 ReadyToAdd 判断错了. 需要重新检查一下

        // TODO: 2019/11/11 需要验证问题的情况

        /*is CanAddFriendResponse.ReadyToAdd -> {
            // TODO: 2019/11/11 这不需要验证信息的情况

            //AddFriendPacket(bot.qqAccount, id, bot.sessionKey, ).sendAndExpectAsync<AddFriendPacket.Response>().await()
            TODO()
        }*/
    }
}

/*
1494695429 同意好友请求后收到以下包:

Mirai 22:11:14 : Packet received: UnknownEventPacket(id=02 10, identity=(1994701021->1994701021))
 = 00 00 00 08 00 0A 00 04 01 00 00 00 00 00 00 06 00 00 00 08 08 02 1A 02 08 44 2A 06 08 83 D8 A5 EE 05
Mirai 22:12:06 : Packet received: UnknownEventPacket(id=02 02, identity=(1994701021->1994701021))
 = 00 00 00 08 00 0A 00 04 01 00 00 00 02 00 00
Mirai 22:12:06 : Packet received: UnknownEventPacket(id=00 D6, identity=(1494695429->1994701021))
 = 00 00 00 08 00 0A 00 04 01 00 00 00 00 00 00 00 BC 01 00 00 00 00 02 00 00
Mirai 22:12:06 : Packet received: UnknownEventPacket(id=00 BC, identity=(1494695429->1994701021))
 = 00 00 00 08 00 0A 00 04 01 00 00 00 01 00 00 0F 00 00 00 00 00 00 00 00 01 03 EB 00 02 0A 00
Mirai 22:12:06 : Packet received: UnknownEventPacket(id=02 10, identity=(1994701021->1994701021))
//�9����同意你的加好友请求"him188的小dick(
 = 00 00 00 08 00 0A 00 04 01 00 00 00 00 00 00 07 00 00 00 3B 08 02 1A 03 08 E2 01 0A 39 08 01 10 85 FC DC C8 05 1A 1B E5 90 8C E6 84 8F E4 BD A0 E7 9A 84 E5 8A A0 E5 A5 BD E5 8F 8B E8 AF B7 E6 B1 82 22 10 68 69 6D 31 38 38 E7 9A 84 E5 B0 8F 64 69 63 6B 28 01
Mirai 22:12:06 : Packet received: UnknownEventPacket(id=02 10, identity=(1994701021->1994701021))
 = 00 00 00 08 00 0A 00 04 01 00 00 00 00 00 00 06 00 00 00 08 08 02 1A 02 08 44 2A 06 08 B7 D8 A5 EE 05
Mirai 22:12:06 : Packet received: UnknownEventPacket(id=02 10, identity=(1994701021->1994701021))
 = 00 00 00 08 00 0A 00 04 01 00 00 00 00 00 00 06 00 00 00 04 08 02 1A 02 08 23 1A 02 08 00
Mirai 22:12:06 : Packet received: UnknownEventPacket(id=02 10, identity=(1994701021->1994701021))
 = 00 00 00 08 00 0A 00 04 01 00 00 00 00 00 00 07 00 00 00 0C 08 02 1A 03 08 E2 01 0A 0A 08 00 10 DD F1 92 B7 07 1A 00
Mirai 22:12:06 : Packet received: UnknownEventPacket(id=02 10, identity=(1994701021->1994701021))
 = 00 00 00 08 00 0A 00 04 01 00 00 00 00 00 00 06 00 00 00 18 08 02 1A 02 08 44 1A 0E 08 DD F1 92 B7 07 10 B7 D8 A5 EE 05 18 01 2A 06 08 B7 D8 A5 EE 05
Mirai 22:12:06 : Packet received: UnknownEventPacket(id=02 10, identity=(1494695429->1994701021))
//来自QQ号查找:BJR
 = 00 00 00 08 00 0A 00 04 01 00 00 00 00 00 00 06 00 00 00 31 08 02 1A 02 08 23 12 2F 08 DD F1 92 B7 07 10 85 FC DC C8 05 18 03 20 82 D8 A5 EE 05 2A 11 E6 9D A5 E8 87 AA 51 51 E5 8F B7 E6 9F A5 E6 89 BE 3A 00 42 00 4A 00 52 00
Mirai 22:12:07 : Packet received: UnknownPacket(01 2D)
body=01 01 00 00 00 01 59 17 3E 05 00 00 00
Mirai 22:12:07 : UnknownPacket(01 2D) = 71 B9 C2 FD 11 79 A3 CC FB 30 BF C6 5E 3B 18 87 A7 1C 52 BF 7F B4 61 42 BF C6 5E 3B 18 87 A7 1C 52 BF 7F B4 61 42 8A CB 49 F4 72 98 29 55 F8 04 FB 38 F5 87 65 98 9D D0 F0 F4 3A EC 12 02 43 F5 39 BF 40 2E 4F 0B 37 29 C6 DB A7 3B FC C7 FB 90 CF 6F 15 D3 34 75 EE 2A 4C 36 E4 39 F2 D1 4B 87 82 37 16 A3 84 E8 D8 2D 19 F3 A8 20 6E 66 C7 22 16 A3 84 E8 D8 2D 19 F3 A8 20 6E 66 C7 22 6D 2A F0 DA A1 E2 C1 54 29 E2 C5 A1 26 11 CA FC 4A E9 EA 32 95 78 41 31 9E 78 04 A8 B6 7C 0E E7 2B 32 87 E3 0A 84 67 F4 83 3F 53 C8 B1 BE EE 07 02 10 97 67 AF 0C DF 2B 20 AC E0 7E 42 7B 98 01 CB CE B8 13 52 8B 34 9A 4D A0 14 BA 6E 88 0E 2F F9 06 B5 1E 4A 00 D7 0E 0A 58 75 7D 39 2E B1 38 A0 4A 13 1C 3E 71 8C 78 CA F7 39 2E B1 38 A0 4A 13 1C 3E 71 8C 78 CA F7 46 06 FA F4 99 D8 52 A1 D7 70 12 40 1B 61 82 3D 8D F6 96 F1 C5 DB 1C E3 F8 9D DD 8A 2C 2C F5 62 EC BF FD C1 F0 77 58 0B FD 29 DE 23 D0 AF CD 46 90 A2 A1 D4 50 6D B2 52 D4 4A 2A EF 7D 4E 6E F8 63 41 BE D8 5F A1 A9 43 BF BC E1 54 C0 A0 33 CD 1B C6 84 2E 72 31 F7 E2 A7 91 3C DB 2D FD A7 84 CA 87 A2 3C 64 A4 04 82 4B 88 74 74 43 45 E1 48 FA BB 15 A6 D5 82 3F FF 2A BA C8 AF F8 E1 77 15 0D 5C 84 EB 40 C7 1E 52 16 CB EB 75 04 54 17 95 09 BF FD CA E8 C7 D1 93 F8 83 6B 50 26 A8 E6 23 00 AA EB 75 56 2D 24 62 CC 79 4E AA 92 B6 F6 CA BA 57 05 57 B3 53 32 60 4B 3B 20 D0 F6 57 31 52 49 EC B0 0B C0 97 D6 39 AC 16 F6 57 31 52 49 EC B0 0B C0 97 D6 39 AC 16 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00

2092749761 同意后收到以下几个包

Mirai 22:04:40 : Packet received: UnknownEventPacket(id=02 10, identity=(1994701021->1994701021))
 = 00 00 00 08 00 0A 00 04 01 00 00 00 00 00 00 06 00 00 00 08 08 02 1A 02 08 44 2A 06 08 F9 D4 A5 EE 05
Mirai 22:04:45 : Packet received: UnknownEventPacket(id=02 10, identity=(1040400290->1994701021))
//5�������%�ԥ� (2对方正在输入...(
 = 00 00 00 08 00 0A 00 04 01 00 00 00 00 00 00 07 00 00 00 35 08 02 1A 03 08 95 02 08 A2 FF 8C F0 03 10 DD F1 92 B7 07 1A 25 08 00 10 05 18 FE D4 A5 EE 05 20 01 28 08 32 15 E5 AF B9 E6 96 B9 E6 AD A3 E5 9C A8 E8 BE 93 E5 85 A5 2E 2E 2E 28 01
Mirai 22:04:45 : Packet received: FriendConversationInitialize(qq=1040400290)
Mirai 22:04:48 : Packet received: UnknownEventPacket(id=00 BC, identity=(2092749761->1994701021))
 = 00 00 00 08 00 0A 00 04 01 00 00 00 01 00 00 0F 00 00 00 00 00 00 00 00 01 03 EB 00 02 0A 00
Mirai 22:04:48 : Packet received: UnknownEventPacket(id=00 D6, identity=(2092749761->1994701021))
 = 00 00 00 08 00 0A 00 04 01 00 00 00 00 00 00 00 BC 01 00 00 00 00 02 00 00
Mirai 22:04:48 : Packet received: UnknownEventPacket(id=02 02, identity=(1994701021->1994701021))
 = 00 00 00 08 00 0A 00 04 01 00 00 00 02 00 00
Mirai 22:04:48 : Packet received: UnknownEventPacket(id=02 10, identity=(1994701021->1994701021))
//:�8����同意你的加好友请求"him188的老公(
 = 00 00 00 08 00 0A 00 04 01 00 00 00 00 00 00 07 00 00 00 3A 08 02 1A 03 08 E2 01 0A 38 08 01 10 C1 A7 F3 E5 07 1A 1B E5 90 8C E6 84 8F E4 BD A0 E7 9A 84 E5 8A A0 E5 A5 BD E5 8F 8B E8 AF B7 E6 B1 82 22 0F 68 69 6D 31 38 38 E7 9A 84 E8 80 81 E5 85 AC 28 01
Mirai 22:04:48 : Packet received: UnknownEventPacket(id=02 10, identity=(1994701021->1994701021))
 = 00 00 00 08 00 0A 00 04 01 00 00 00 00 00 00 06 00 00 00 08 08 02 1A 02 08 44 2A 06 08 81 D5 A5 EE 05
Mirai 22:04:48 : Packet received: UnknownPacket(01 2D)
body=01 01 00 00 00 01 7C BC D3 C1 00 00 00
Mirai 22:04:48 : UnknownPacket(01 2D) = 66 B9 2C A3 EA 31 AD 5B E4 52 37 25 3B 21 A9 FF EE 2E 80 AF 6F 9B C0 A7 37 25 3B 21 A9 FF EE 2E 80 AF 6F 9B C0 A7 6D FF BF 73 37 8A 34 4C D1 DF D3 ED 93 DA B3 32 AC 4D 11 D7 68 F0 93 A3 F5 BE 93 DA B3 32 AC 4D 11 D7 68 F0 93 A3 F5 BE CC 5F 8A DC 52 D7 2E 39 F6 D0 A6 FE E2 FE A9 1D C1 5E AC 2A 02 46 A0 90 23 6E 10 A6 48 B0 04 BC 06 F8 AF 42 87 DA 69 42 55 AA 48 B0 04 BC 06 F8 AF 42 87 DA 69 42 55 AA 29 3B C5 0C 5B 43 EE FA 30 EA 81 86 F3 8D FB 41 EA A3 23 59 27 49 03 B6 34 5C 4A CA DE 8E 3C 02 36 5F 48 C7 14 73 E4 D4 D8 C3 81 80 FC 49 9B 3C CC 4D D8 E9 07 84 56 40 F9 7E 80 D8 11 60 B1 FF F0 0E 5D 6E 4B 45 41 B4 81 54 EB B9 EE 98 D2 29 F3 05 BD 96 D3 E4 A6 42 98 CD C4 D1 5F 10 DE 62 EB E5 D3 E4 A6 42 98 CD C4 D1 5F 10 DE 62 EB E5 25 61 AA 54 A1 BE 14 78 F9 AC 2B F1 43 0B B5 51 2D 15 AA DE 97 B8 CC A3 2A 9B 8B AB 37 7C 45 57 D6 B9 BF 6C 4B 7B 66 AD 89 EB 90 42 0F 5F 63 A7 CC 06 4D 08 E0 5C 5D E3 9A AF 0D 19 C7 78 B5 30 6C 9D E2 A4 CA 3A DD 64 FC 78 A8 E1 59 1F 67 97 C6 B2 0B 73 EB 9A 2D 75 07 7E CE 82 3B EC CF 3A 9F 98 4F C0 BA 98 69 D7 65 87 EA 53 90 18 01 BD 8B AB EB 40 74 9C 03 C4 92 3B 9A F5 3A DD 51 84 EF 72 48 71 DC B4 AA D5 95 AB BC 4B 97 70 4D FD EE DE 37 BD 33 0C DF 64 C5 55 2E ED A6 98 6A 88 28 8B F3 24 8D 73 00 DE 9E FC 78 15 4A AC E2 3F AD 93 4C 2F 88 48 34 DA F3 F7 FC B7 E7 39 F6 33 3E 5C 88 48 34 DA F3 F7 FC B7 E7 39 F6 33 3E 5C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00


 */