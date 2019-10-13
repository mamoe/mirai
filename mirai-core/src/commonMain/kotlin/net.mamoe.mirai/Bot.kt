package net.mamoe.mirai

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import net.mamoe.mirai.Bot.ContactSystem
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.TIMBotNetworkHandler
import net.mamoe.mirai.utils.BotAccount
import net.mamoe.mirai.utils.ContactList
import net.mamoe.mirai.utils.MiraiLogger

/**
 * Mirai 的机器人. 一个机器人实例登录一个 QQ 账号.
 * Mirai 为多账号设计, 可同时维护多个机器人.
 *
 * [Bot] 由 3 个模块组成.
 * [联系人管理][ContactSystem]: 可通过 [Bot.contacts] 访问
 * [网络处理器][TIMBotNetworkHandler]: 可通过 [Bot.network] 访问
 * [机器人账号信息][BotAccount]: 可通过 [Bot.account] 访问
 *
 * 若你需要得到机器人的 QQ 账号, 请访问 [Bot.account]
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
 * To of all the QQ contacts, access [Bot.account]
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

    val network: BotNetworkHandler<*> = TIMBotNetworkHandler(this)

    init {
        instances.add(this)

        this.logger.identity = "Bot" + this.id + "(" + this.account.qqNumber + ")"
    }

    override fun toString(): String = "Bot{id=$id,qq=${account.qqNumber}}"

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
        fun getQQ(qqNumber: Long): QQ = qqs.getOrPut(qqNumber) { QQ(this@Bot, qqNumber) }

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