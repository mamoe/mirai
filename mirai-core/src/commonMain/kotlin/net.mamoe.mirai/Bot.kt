package net.mamoe.mirai

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import net.mamoe.mirai.Bot.ContactSystem
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.contact.groupIdToNumber
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.TIMBotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginResult
import net.mamoe.mirai.utils.*
import kotlin.jvm.JvmOverloads

/**
 * Mirai 的机器人. 一个机器人实例登录一个 QQ 账号.
 * Mirai 为多账号设计, 可同时维护多个机器人.
 *
 * [Bot] 由 3 个模块组成.
 * [联系人管理][ContactSystem]: 可通过 [Bot.contacts] 访问
 * [网络处理器][TIMBotNetworkHandler]: 可通过 [Bot.network] 访问
 * [机器人账号信息][BotAccount]: 可通过 [Bot.qqAccount] 访问
 *
 * 若你需要得到机器人的 QQ 账号, 请访问 [Bot.qqAccount]
 * 若你需要得到服务器上所有机器人列表, 请访问 [Bot.instances]
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
 * @author NatrualHG
 * @see net.mamoe.mirai.contact.Contact
 */
class Bot(val account: BotAccount, val logger: MiraiLogger) {
    val id = nextId()

    val contacts = ContactSystem()

    var network: BotNetworkHandler<*> = TIMBotNetworkHandler(this)

    init {
        instances.add(this)

        this.logger.identity = "Bot" + this.id + "(" + this.account.account + ")"
    }

    override fun toString(): String = "Bot{id=$id,qq=${account.account}}"

    /**
     * [关闭][BotNetworkHandler.close]网络处理器, 取消所有运行在 [BotNetworkHandler.NetworkScope] 下的协程.
     * 然后重新启动并尝试登录
     */
    @JvmOverloads
    suspend fun reinitializeNetworkHandler(configuration: BotNetworkConfiguration, cause: Throwable? = null): LoginResult {
        logger.logPurple("Reinitializing BotNetworkHandler")
        try {
            network.close(cause)
        } catch (e: Exception) {
            e.log()
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
        val groups = ContactList<Group>()
        private val groupsLock = Mutex()
        val qqs = ContactList<QQ>()
        private val qqsLock = Mutex()

        /**
         * 通过群号码获取群对象.
         * 注意: 在并发调用时, 这个方法并不是原子的.
         */
        fun getQQ(account: Long): QQ = qqs.getOrPut(account) { QQ(this@Bot, account) }

        /**
         * 通过群号码获取群对象.
         * 注意: 在并发调用时, 这个方法并不是原子的.
         */
        fun getGroupByNumber(groupNumber: Long): Group = groups.getOrPut(groupNumber) { Group(this@Bot, groupNumber) }


        fun getGroupById(groupId: Long): Group {
            return getGroupByNumber(Group.groupIdToNumber(groupId))
        }
    }

    fun close() {
        this.network.close()
        this.contacts.groups.clear()
        this.contacts.qqs.clear()
    }

    companion object {
        val instances: MutableList<Bot> = mutableListOf()

        private val id = atomic(0)
        fun nextId(): Int = id.addAndGet(1)
    }
}